extern crate ff;
extern crate jni;
extern crate pairing;
extern crate libc;
extern crate rand;
extern crate sapling_crypto;
extern crate blake2_rfc;
extern crate bellman;
extern crate crypto_api_chachapoly;
extern crate zcash_client_backend;
extern crate zcash_primitives;
extern crate zcash_proofs;
extern crate zip32;

mod hashreader;

#[macro_use]
extern crate lazy_static;

use crypto_api_chachapoly::{ChaCha20Ietf, ChachaPolyIetf};
use ff::{PrimeField, PrimeFieldRepr, Field};
use pairing::bls12_381::{Bls12, Fr, FrRepr};
use pairing::{Engine};
use bellman::{Circuit, SynthesisError, ConstraintSystem};

use sapling_crypto::{
    circuit::{
        multipack,
        sapling::{Output, Spend},
    },
    jubjub::{edwards, fs::{Fs, FsRepr}, FixedGenerators, JubjubBls12, ToUniform, Unknown, JubjubEngine},
    primitives::{Diversifier, Note, PaymentAddress, ProofGenerationKey, ValueCommitment, ViewingKey},
    redjubjub::{self, PrivateKey, PublicKey, Signature},
};

use zcash_primitives::{
    keys::{prf_expand, prf_expand_vec, ExpandedSpendingKey, FullViewingKey, OutgoingViewingKey},
    merkle_tree::CommitmentTreeWitness,
    sapling::{merkle_hash, spend_sig},
    JUBJUB,
};

use zcash_proofs::{
    load_parameters,
    sapling::{SaplingProvingContext, SaplingVerificationContext, compute_value_balance},
};

use sapling_crypto::circuit::sprout::{self, TREE_DEPTH as SPROUT_TREE_DEPTH};

use bellman::groth16::{
    create_random_proof, generate_random_parameters, prepare_verifying_key, verify_proof, Parameters, PreparedVerifyingKey,
    Proof, VerifyingKey,
};

use rand::{OsRng, Rand, Rng, SeedableRng, XorShiftRng, thread_rng};
use std::io::{self, BufReader};
use std::ffi::CStr;

use libc::{c_char, c_uchar, int64_t, size_t, uint32_t, uint64_t};

use jni::{
    objects::{JClass, JString},
    sys::{jboolean, jbyteArray, jint, jlong, jobjectArray, jstring, JNI_FALSE, JNI_TRUE},
    JNIEnv,
};

use zcash_client_backend::{
    constants::{
    HRP_SAPLING_EXTENDED_SPENDING_KEY_MAIN,
    HRP_SAPLING_EXTENDED_SPENDING_KEY_TEST,
    HRP_SAPLING_PAYMENT_ADDRESS_MAIN,
    HRP_SAPLING_PAYMENT_ADDRESS_TEST
    },
    encoding::{
            decode_extended_spending_key, decode_payment_address, encode_extended_spending_key, encode_payment_address,
    },
};

use zip32::{ChildIndex, ExtendedFullViewingKey, ExtendedSpendingKey};

static mut SAPLING_OUTPUT_PARAMS: Option<Parameters<Bls12>> = None;
static mut SAPLING_OUTPUT_VK: Option<PreparedVerifyingKey<Bls12>> = None;
static mut SAPLING_SPEND_PARAMS: Option<Parameters<Bls12>> = None;
static mut SAPLING_SPEND_VK: Option<PreparedVerifyingKey<Bls12>> = None;

const GROTH_PROOF_SIZE: usize = 48 // π_A
    + 96 // π_B
    + 48; // π_C

const COMPACT_NOTE_SIZE: usize = (
    1  + // version
    11 + // diversifier
    8  + // value
    32
    // rcv
);

const NOTE_PLAINTEXT_SIZE: usize = COMPACT_NOTE_SIZE + 512;
const SAPLING_TREE_DEPTH: usize = 32;
const ENC_CIPHERTEXT_SIZE: usize = NOTE_PLAINTEXT_SIZE + 16;

fn is_small_order<Order>(p: &edwards::Point<Bls12, Order>) -> bool {
    p.double(&JUBJUB).double(&JUBJUB).double(&JUBJUB) == edwards::Point::zero()
}

/// Writes an FrRepr to [u8] of length 32
fn write_le(f: FrRepr, to: &mut [u8]) {
    assert_eq!(to.len(), 32);

    f.write_le(to).expect("length is 32 bytes");
}

/// Reads an FrRepr from a [u8] of length 32.
/// This will panic (abort) if length provided is
/// not correct.
fn read_le(from: &[u8]) -> FrRepr {
    assert_eq!(from.len(), 32);

    let mut f = FrRepr::default();
    f.read_le(from).expect("length is 32 bytes");

    f
}

/// Reads an FsRepr from [u8] of length 32
/// This will panic (abort) if length provided is
/// not correct
fn read_fs(from: &[u8]) -> FsRepr {
    assert_eq!(from.len(), 32);

    let mut f = <<Bls12 as JubjubEngine>::Fs as PrimeField>::Repr::default();
    f.read_le(from).expect("length is 32 bytes");

    f
}

// Private utility function to get Note from C parameters
fn priv_get_note(
    diversifier: *const [c_uchar; 11],
    pk_d: *const [c_uchar; 32],
    value: uint64_t,
    r: *const [c_uchar; 32],
) -> Result<sapling_crypto::primitives::Note<Bls12>, ()> {
    let diversifier = sapling_crypto::primitives::Diversifier(unsafe { *diversifier });
    let g_d = match diversifier.g_d::<Bls12>(&JUBJUB) {
        Some(g_d) => g_d,
        None => return Err(()),
    };

    let pk_d = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*pk_d })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return Err(()),
    };

    let pk_d = match pk_d.as_prime_order(&JUBJUB) {
        Some(pk_d) => pk_d,
        None => return Err(()),
    };

    // Deserialize randomness
    let r = match Fs::from_repr(read_fs(&(unsafe { &*r })[..])) {
        Ok(r) => r,
        Err(_) => return Err(()),
    };

    let note = sapling_crypto::primitives::Note {
        value,
        g_d,
        pk_d,
        r,
    };

    Ok(note)
}

/// Return 32 byte random scalar, uniformly.
#[no_mangle]
pub extern "system" fn librustzcash_sapling_generate_r(result: *mut [c_uchar; 32]) {
    // create random 64 byte buffer
    let mut rng = OsRng::new().expect("should be able to construct RNG");
    let mut buffer = [0u8; 64];
    for i in 0..buffer.len() {
        buffer[i] = rng.gen();
    }

    // reduce to uniform value
    let r = <Bls12 as JubjubEngine>::Fs::to_uniform(&buffer[..]);
    let result = unsafe { &mut *result };
    r.into_repr()
        .write_le(&mut result[..])
        .expect("result must be 32 bytes");
}

#[no_mangle]
pub extern "system" fn librustzcash_init_zksnark_params(
    output_bytes: *const [c_uchar; 3592860],
    output_hash: *const c_char,
    spend_bytes: *const [c_uchar; 47958396],
    spend_hash: *const c_char,
) {

    init_zksnark_params(
        output_bytes,
        output_hash,
        spend_bytes,
        spend_hash
    )
}

fn init_zksnark_params(
    output_bytes: *const [c_uchar; 3592860],
    output_hash: *const c_char,
    spend_bytes: *const [c_uchar; 47958396],
    spend_hash: *const c_char,
) {
    // Initialize jubjub parameters here
    lazy_static::initialize(&JUBJUB);

    let output_hash = unsafe { CStr::from_ptr(output_hash) }
            .to_str()
            .expect("hash should be a valid string")
            .to_string();

    let spend_hash = unsafe { CStr::from_ptr(spend_hash) }
                .to_str()
                .expect("hash should be a valid string")
                .to_string();

    let mut output_fs =
        hashreader::HashReader::new(BufReader::with_capacity(1024 * 1024, &(unsafe { &*output_bytes })[..]));
    let mut spend_fs =
            hashreader::HashReader::new(BufReader::with_capacity(1024 * 1024, &(unsafe { &*spend_bytes })[..]));



    let output_params = Parameters::<Bls12>::read(&mut output_fs, false)
            .expect("couldn't deserialize Sapling spend parameters file");
    let spend_params = Parameters::<Bls12>::read(&mut spend_fs, false)
                .expect("couldn't deserialize Sapling spend parameters file");

    // There is extra stuff (the transcript) at the end of the parameter file which is
        // used to verify the parameter validity, but we're not interested in that. We do
        // want to read it, though, so that the BLAKE2b computed afterward is consistent
        // with `b2sum` on the files.
    let mut sink = io::sink();
    io::copy(&mut output_fs, &mut sink)
        .expect("couldn't finish reading Sapling output parameter file");
    io::copy(&mut spend_fs, &mut sink)
            .expect("couldn't finish reading Sapling spend parameter file");

    if output_fs.into_hash() != output_hash {
        panic!("Sapling output parameter file is not correct, please clean your `~/.zcash-params/` and re-run `fetch-params`.");
    }
    if spend_fs.into_hash() != spend_hash {
            panic!("Sapling spend parameter file is not correct, please clean your `~/.zcash-params/` and re-run `fetch-params`.");
    }

    let spend_vk = prepare_verifying_key(&spend_params.vk);

    unsafe {
        SAPLING_OUTPUT_PARAMS = Some(output_params);
        SAPLING_SPEND_PARAMS = Some(spend_params);
        SAPLING_SPEND_VK = Some(spend_vk);
    }
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_ka_derivepublic(
    diversifier: *const [c_uchar; 11],
    esk: *const [c_uchar; 32],
    result: *mut [c_uchar; 32],
) -> bool {
    let diversifier = sapling_crypto::primitives::Diversifier(unsafe { *diversifier });

    // Compute g_d from the diversifier
    let g_d = match diversifier.g_d::<Bls12>(&JUBJUB) {
        Some(g) => g,
        None => return false,
    };

    // Deserialize esk
    let esk = match Fs::from_repr(read_fs(&(unsafe { &*esk })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    let p = g_d.mul(esk, &JUBJUB);

    let result = unsafe { &mut *result };
    p.write(&mut result[..]).expect("length is not 32 bytes");

    true
}

/// Compute Sapling note commitment.
#[no_mangle]
pub extern "system" fn librustzcash_sapling_compute_cm(
    diversifier: *const [c_uchar; 11],
    pk_d: *const [c_uchar; 32],
    value: uint64_t,
    r: *const [c_uchar; 32],
    result: *mut [c_uchar; 32],
) -> bool {
    let note = match priv_get_note(diversifier, pk_d, value, r) {
        Ok(p) => p,
        Err(_) => return false,
    };

    let result = unsafe { &mut *result };
    write_le(note.cm(&JUBJUB).into_repr(), &mut result[..]);

    true
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_output_proof(
    ctx: *mut SaplingProvingContext,
    esk: *const [c_uchar; 32],
    diversifier: *const [c_uchar; 11],
    pk_d: *const [c_uchar; 32],
    rcm: *const [c_uchar; 32],
    value: uint64_t,
    cv: *mut [c_uchar; 32],
    zkproof: *mut [c_uchar; GROTH_PROOF_SIZE],
) -> bool {
    // Grab `esk`, which the caller should have constructed for the DH key exchange.
    let esk = match Fs::from_repr(read_fs(&(unsafe { &*esk })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // Grab the diversifier from the caller.
    let diversifier = sapling_crypto::primitives::Diversifier(unsafe { *diversifier });

    // Grab pk_d from the caller.
    let pk_d = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*pk_d })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // pk_d should be prime order.
    let pk_d = match pk_d.as_prime_order(&JUBJUB) {
        Some(p) => p,
        None => return false,
    };

    // Construct a payment address
    let payment_address = sapling_crypto::primitives::PaymentAddress {
        pk_d: pk_d,
        diversifier: diversifier,
    };

    // Initialize secure RNG
    let mut rng = OsRng::new().expect("should be able to construct RNG");

    // The caller provides the commitment randomness for the output note
    let rcm = match Fs::from_repr(read_fs(&(unsafe { &*rcm })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // We construct ephemeral randomness for the value commitment. This
    // randomness is not given back to the caller, but the synthetic
    // blinding factor `bsk` is accumulated in the context.
    let rcv = Fs::rand(&mut rng);

    // Accumulate the value commitment randomness in the context
    {
        let mut tmp = rcv.clone();
        tmp.negate(); // Outputs subtract from the total.
        tmp.add_assign(&unsafe { &*ctx }.bsk);

        // Update the context
        unsafe { &mut *ctx }.bsk = tmp;
    }

    // Construct the value commitment for the proof instance
    let value_commitment = sapling_crypto::primitives::ValueCommitment::<Bls12> {
        value: value,
        randomness: rcv,
    };

    // We now have a full witness for the output proof.
    let instance = sapling_crypto::circuit::sapling::Output {
        params: &*JUBJUB,
        value_commitment: Some(value_commitment.clone()),
        payment_address: Some(payment_address.clone()),
        commitment_randomness: Some(rcm),
        esk: Some(esk.clone()),
    };

    // Create proof
    let proof = create_random_proof(
        instance,
        unsafe { SAPLING_OUTPUT_PARAMS.as_ref() }.unwrap(),
        &mut rng,
    ).expect("proving should not fail");

    // Write the proof out to the caller
    proof
        .write(&mut (unsafe { &mut *zkproof })[..])
        .expect("should be able to serialize a proof");

    // Compute the value commitment
    let value_commitment: edwards::Point<Bls12, Unknown> = value_commitment.cm(&JUBJUB).into();

    // Accumulate the value commitment in the context. We do this to check internal consistency.
    {
        let mut tmp = value_commitment.clone();
        tmp = tmp.negate(); // Outputs subtract from the total.
        tmp = tmp.add(&unsafe { &*ctx }.bvk, &JUBJUB);

        // Update the context
        unsafe { &mut *ctx }.bvk = tmp;
    }

    // Write the value commitment to the caller
    value_commitment
        .write(&mut (unsafe { &mut *cv })[..])
        .expect("should be able to serialize rcv");

    true
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_binding_sig(
    ctx: *const SaplingProvingContext,
    value_balance: int64_t,
    sighash: *const [c_uchar; 32],
    result: *mut [c_uchar; 64],
) -> bool {
    // Grab the current `bsk` from the context
    let bsk = redjubjub::PrivateKey::<Bls12>(unsafe { &*ctx }.bsk);

    // Grab the `bvk` using DerivePublic.
    let bvk = redjubjub::PublicKey::from_private(
        &bsk,
        FixedGenerators::ValueCommitmentRandomness,
        &JUBJUB,
    );

    // In order to check internal consistency, let's use the accumulated value
    // commitments (as the verifier would) and apply valuebalance to compare
    // against our derived bvk.
    {
        // Compute value balance
        let mut value_balance = match compute_value_balance(value_balance, &JUBJUB) {
            Some(a) => a,
            None => return false,
        };

        // Subtract value_balance from current bvk to get final bvk
        value_balance = value_balance.negate();
        let mut tmp = unsafe { &*ctx }.bvk.clone();
        tmp = tmp.add(&value_balance, &JUBJUB);

        // The result should be the same, unless the provided valueBalance is wrong.
        if bvk.0 != tmp {
            return false;
        }
    }

    // Construct signature message
    let mut data_to_be_signed = [0u8; 64];
    bvk.0
        .write(&mut data_to_be_signed[0..32])
        .expect("message buffer should be 32 bytes");
    (&mut data_to_be_signed[32..64]).copy_from_slice(&(unsafe { &*sighash })[..]);

    // Sign
    let mut rng = OsRng::new().expect("should be able to construct RNG");
    let sig = bsk.sign(
        &data_to_be_signed,
        &mut rng,
        FixedGenerators::ValueCommitmentRandomness,
        &JUBJUB,
    );

    // Write out signature
    sig.write(&mut (unsafe { &mut *result })[..])
        .expect("result should be 64 bytes");

    true
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_ka_agree(
    p: *const [c_uchar; 32],
    sk: *const [c_uchar; 32],
    result: *mut [c_uchar; 32],
) -> bool {
    // Deserialize p
    let p = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*p })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // Deserialize sk
    let sk = match Fs::from_repr(read_fs(&(unsafe { &*sk })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // Multiply by 8
    let p = p.mul_by_cofactor(&JUBJUB);

    // Multiply by sk
    let p = p.mul(sk, &JUBJUB);

    // Produce result
    let result = unsafe { &mut *result };
    p.write(&mut result[..]).expect("length is not 32 bytes");

    true
}

#[no_mangle]
pub extern "system" fn librustzcash_ivk_to_pkd(
    ivk: *const [c_uchar; 32],
    diversifier: *const [c_uchar; 11],
    result: *mut [c_uchar; 32],
) -> bool {
    let ivk = read_fs(unsafe { &*ivk });
    let diversifier = sapling_crypto::primitives::Diversifier(unsafe { *diversifier });
    if let Some(g_d) = diversifier.g_d::<Bls12>(&JUBJUB) {
        let pk_d = g_d.mul(ivk, &JUBJUB);

        let result = unsafe { &mut *result };

        pk_d.write(&mut result[..]).expect("length is 32 bytes");

        true
    } else {
        false
    }
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_check_output(
    ctx: *mut SaplingVerificationContext,
    cv: *const [c_uchar; 32],
    cm: *const [c_uchar; 32],
    epk: *const [c_uchar; 32],
    zkproof: *const [c_uchar; GROTH_PROOF_SIZE],
) -> u32 {
//lazy_static::initialize(&JUBJUB);

let mut res: u32 = 0;
res = 1;
    // Deserialize the value commitment
    let cv = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*cv })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return res,
    };
res = 2;
    if is_small_order(&cv) {
        return res;
    }

    // Accumulate the value commitment in the context
    {
        let mut tmp = cv.clone();
        tmp = tmp.negate(); // Outputs subtract from the total.
        tmp = tmp.add(&unsafe { &*ctx }.bvk, &JUBJUB);

        // Update the context
        unsafe { &mut *ctx }.bvk = tmp;
    }

res = 3;
    // Deserialize the commitment, which should be an element
    // of Fr.
    let cm = match Fr::from_repr(read_le(&(unsafe { &*cm })[..])) {
        Ok(a) => a,
        Err(_) => return res,
    };
res = 4;
    // Deserialize the ephemeral key
    let epk = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*epk })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return res,
    };
res = 5;
    if is_small_order(&epk) {
        return res;
    }

    // Construct public input for circuit
    let mut public_input = [Fr::zero(); 5];
    {
        let (x, y) = cv.into_xy();
        public_input[0] = x;
        public_input[1] = y;
    }
    {
        let (x, y) = epk.into_xy();
        public_input[2] = x;
        public_input[3] = y;
    }
    public_input[4] = cm;
res = 6;
    // Deserialize the proof
    let zkproof = match Proof::<Bls12>::read(&(unsafe { &*zkproof })[..]) {
        Ok(p) => p,
        Err(_) => return res,
    };
res = 7;


    res
}

#[no_mangle]
pub extern "system" fn librustzcash_merkle_hash(
    depth: size_t,
    a: *const [c_uchar; 32],
    b: *const [c_uchar; 32],
    result: *mut [c_uchar; 32],
) {
    // Should be okay, because caller is responsible for ensuring
    // the pointer is a valid pointer to 32 bytes, and that is the
    // size of the representation
    let a_repr = read_le(unsafe { &(&*a)[..] });

    // Should be okay, because caller is responsible for ensuring
    // the pointer is a valid pointer to 32 bytes, and that is the
    // size of the representation
    let b_repr = read_le(unsafe { &(&*b)[..] });

    let tmp = merkle_hash(depth, &a_repr, &b_repr);

    // Should be okay, caller is responsible for ensuring the pointer
    // is a valid pointer to 32 bytes that can be mutated.
    let result = unsafe { &mut *result };

    write_le(tmp, &mut result[..]);
}

#[no_mangle]
pub extern "system" fn librustzcash_tree_uncommitted(result: *mut [c_uchar; 32]) {
    let tmp = sapling_crypto::primitives::Note::<Bls12>::uncommitted().into_repr();

    // Should be okay, caller is responsible for ensuring the pointer
    // is a valid pointer to 32 bytes that can be mutated.
    let result = unsafe { &mut *result };

    write_le(tmp, &mut result[..]);
}

/// Compute Sapling note nullifier.
#[no_mangle]
pub extern "system" fn librustzcash_sapling_compute_nf(
    diversifier: *const [c_uchar; 11],
    pk_d: *const [c_uchar; 32],
    value: uint64_t,
    r: *const [c_uchar; 32],
    ak: *const [c_uchar; 32],
    nk: *const [c_uchar; 32],
    position: uint64_t,
    result: *mut [c_uchar; 32],
) -> bool {
    let note = match priv_get_note(diversifier, pk_d, value, r) {
        Ok(p) => p,
        Err(_) => return false,
    };

    let ak = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*ak })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return false,
    };

    let ak = match ak.as_prime_order(&JUBJUB) {
        Some(ak) => ak,
        None => return false,
    };

    let nk = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*nk })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return false,
    };

    let nk = match nk.as_prime_order(&JUBJUB) {
        Some(nk) => nk,
        None => return false,
    };

    let vk = ViewingKey { ak, nk };
    let nf = note.nf(&vk, position, &JUBJUB);
    let result = unsafe { &mut *result };
    result.copy_from_slice(&nf);

    true
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_spend_proof(
    ctx: *mut SaplingProvingContext,
    ak: *const [c_uchar; 32],
    nsk: *const [c_uchar; 32],
    diversifier: *const [c_uchar; 11],
    rcm: *const [c_uchar; 32],
    ar: *const [c_uchar; 32],
    value: uint64_t,
    anchor: *const [c_uchar; 32],
    witness: *const [c_uchar; 1 + 33 * SAPLING_TREE_DEPTH + 8],
    cv: *mut [c_uchar; 32],
    rk_out: *mut [c_uchar; 32],
    zkproof: *mut [c_uchar; GROTH_PROOF_SIZE],
    nf: *mut [c_uchar; 32],
) -> bool {
    // Grab `ak` from the caller, which should be a point.
    let ak = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*ak })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // `ak` should be prime order.
    let ak = match ak.as_prime_order(&JUBJUB) {
        Some(p) => p,
        None => return false,
    };

    // Grab `nsk` from the caller
    let nsk = match Fs::from_repr(read_fs(&(unsafe { &*nsk })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // Construct the proof generation key
    let proof_generation_key = ProofGenerationKey {
        ak: ak.clone(),
        nsk,
    };

    // Grab the diversifier from the caller
    let diversifier = sapling_crypto::primitives::Diversifier(unsafe { *diversifier });

    // The caller chooses the note randomness
    let rcm = match Fs::from_repr(read_fs(&(unsafe { &*rcm })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // The caller also chooses the re-randomization of ak
    let ar = match Fs::from_repr(read_fs(&(unsafe { &*ar })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // We need to compute the anchor of the Spend.
    let anchor = match Fr::from_repr(read_le(unsafe { &(&*anchor)[..] })) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // The witness contains the incremental tree witness information, in a
    // weird serialized format.
    let witness = match CommitmentTreeWitness::from_slice(unsafe { &(&*witness)[..] }) {
        Ok(w) => w,
        Err(_) => return false,
    };

    // Create proof
    let (proof, value_commitment, rk, nullifier) = unsafe { &mut *ctx }
        .spend_proof(
            proof_generation_key,
            diversifier,
            rcm,
            ar,
            value,
            anchor,
            witness,
            unsafe { SAPLING_SPEND_PARAMS.as_ref() }.unwrap(),
            unsafe { SAPLING_SPEND_VK.as_ref() }.unwrap(),
            &JUBJUB,
        )
        .expect("proving should not fail");

    // Write value commitment to caller
    value_commitment
        .write(&mut unsafe { &mut *cv }[..])
        .expect("should be able to serialize cv");

    // Write proof out to caller
    proof
        .write(&mut (unsafe { &mut *zkproof })[..])
        .expect("should be able to serialize a proof");

    // Write out `rk` to the caller
    rk.write(&mut unsafe { &mut *rk_out }[..])
        .expect("should be able to write to rk_out");

    // Write out `nullifier` to the caller
    let nf = unsafe { &mut *nf };
        nf.copy_from_slice(&nullifier);

    true
}

#[no_mangle]
pub extern "system" fn encrypt_note_plaintext(
    key: [c_uchar; 32],
    enc_ciphertext: [c_uchar; NOTE_PLAINTEXT_SIZE],
    result: *mut [c_uchar; ENC_CIPHERTEXT_SIZE],
) {

            let mut input = enc_ciphertext.to_vec();
            //input.extend_from_slice(&enc_ciphertext[0..NOTE_PLAINTEXT_SIZE]);
            assert_eq!(input.len(), NOTE_PLAINTEXT_SIZE);

            let mut output = [0u8; ENC_CIPHERTEXT_SIZE];
            assert_eq!(
                ChachaPolyIetf::aead_cipher()
                    .seal_to(&mut output, &input, &[], &key, &[0u8; 12])
                    .unwrap(),
                ENC_CIPHERTEXT_SIZE
            );

            let result = unsafe { &mut *result };
            result.copy_from_slice(&output);
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_spend_sig(
    ask: *const [c_uchar; 32],
    ar: *const [c_uchar; 32],
    sighash: *const [c_uchar; 32],
    result: *mut [c_uchar; 64],
) -> bool {
    // The caller provides the re-randomization of `ak`.
    let ar = match Fs::from_repr(read_fs(&(unsafe { &*ar })[..])) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // The caller provides `ask`, the spend authorizing key.
    let ask = match redjubjub::PrivateKey::<Bls12>::read(&(unsafe { &*ask })[..]) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // Do the signing
    let sig = spend_sig(ask, ar, unsafe { &*sighash }, &JUBJUB);

    // Write out the signature
    sig.write(&mut (unsafe { &mut *result })[..])
        .expect("result should be 64 bytes");

    true
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_check_spend(
    ctx: *mut SaplingVerificationContext,
    cv: *const [c_uchar; 32],
    anchor: *const [c_uchar; 32],
    nullifier: *const [c_uchar; 32],
    rk: *const [c_uchar; 32],
    zkproof: *const [c_uchar; GROTH_PROOF_SIZE],
    spend_auth_sig: *const [c_uchar; 64],
    sighash_value: *const [c_uchar; 32],
) -> bool {
    // Deserialize the value commitment
    let cv = match edwards::Point::<Bls12, Unknown>::read(&(unsafe { &*cv })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // Deserialize the anchor, which should be an element
    // of Fr.
    let anchor = match Fr::from_repr(read_le(&(unsafe { &*anchor })[..])) {
        Ok(a) => a,
        Err(_) => return false,
    };

    // Deserialize rk
    let rk = match redjubjub::PublicKey::<Bls12>::read(&(unsafe { &*rk })[..], &JUBJUB) {
        Ok(p) => p,
        Err(_) => return false,
    };

    // Deserialize the signature
    let spend_auth_sig = match Signature::read(&(unsafe { &*spend_auth_sig })[..]) {
        Ok(sig) => sig,
        Err(_) => return false,
    };

    // Deserialize the proof
    let zkproof = match Proof::<Bls12>::read(&(unsafe { &*zkproof })[..]) {
        Ok(p) => p,
        Err(_) => return false,
    };

    unsafe { &mut *ctx }.check_spend(
        cv,
        anchor,
        unsafe { &*nullifier },
        rk,
        unsafe { &*sighash_value },
        spend_auth_sig,
        zkproof,
        unsafe { SAPLING_SPEND_VK.as_ref() }.unwrap(),
        &JUBJUB,
    )
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_final_check(
    ctx: *mut SaplingVerificationContext,
    value_balance: int64_t,
    binding_sig: *const [c_uchar; 64],
    sighash_value: *const [c_uchar; 32],
) -> bool {
    // Deserialize the signature
    let binding_sig = match Signature::read(&(unsafe { &*binding_sig })[..]) {
        Ok(sig) => sig,
        Err(_) => return false,
    };

    unsafe { &*ctx }.final_check(
        value_balance,
        unsafe { &*sighash_value },
        binding_sig,
        &JUBJUB,
    )
}

#[no_mangle]
pub extern "system" fn librustzcash_test_verify() {

                let rng = &mut thread_rng();

                let params = generate_random_parameters::<Bls12, _, _>(
                    MySillyCircuit { a: None, b: None },
                    rng
                ).unwrap();

                {
                    let mut v = vec![];

                    params.write(&mut v).unwrap();
                    assert_eq!(v.len(), 2136);

                    let de_params = Parameters::read(&v[..], true).unwrap();
                    assert!(params == de_params);

                    let de_params = Parameters::read(&v[..], false).unwrap();
                    assert!(params == de_params);
                }

                let pvk = prepare_verifying_key::<Bls12>(&params.vk);

                let a = Fr::rand(rng);
                let b = Fr::rand(rng);
                let mut c = a;
                c.mul_assign(&b);

                let proof = create_random_proof(
                    MySillyCircuit {
                        a: Some(a),
                        b: Some(b)
                    },
                    &params,
                    rng
                ).unwrap();

                let mut v = vec![];
                proof.write(&mut v).unwrap();

                assert_eq!(v.len(), 192);

                let de_proof = Proof::read(&v[..]).unwrap();
                assert!(proof == de_proof);

                assert!(verify_proof(&pvk, &proof, &[c]).unwrap());
                assert!(!verify_proof(&pvk, &proof, &[a]).unwrap());

}

struct MySillyCircuit<E: Engine> {
            a: Option<E::Fr>,
            b: Option<E::Fr>
}

impl<E: Engine> Circuit<E> for MySillyCircuit<E> {
            fn synthesize<CS: ConstraintSystem<E>>(
                self,
                cs: &mut CS
            ) -> Result<(), SynthesisError>
            {
                let a = cs.alloc(|| "a", || self.a.ok_or(SynthesisError::AssignmentMissing))?;
                let b = cs.alloc(|| "b", || self.b.ok_or(SynthesisError::AssignmentMissing))?;
                let c = cs.alloc_input(|| "c", || {
                    let mut a = self.a.ok_or(SynthesisError::AssignmentMissing)?;
                    let b = self.b.ok_or(SynthesisError::AssignmentMissing)?;

                    a.mul_assign(&b);
                    Ok(a)
                })?;

                cs.enforce(
                    || "a*b=c",
                    |lc| lc + a,
                    |lc| lc + b,
                    |lc| lc + c
                );

                Ok(())
            }
        }


#[no_mangle]
pub extern "system" fn librustzcash_sapling_proving_ctx_init() -> *mut SaplingProvingContext {
    let ctx = Box::new(SaplingProvingContext {
        bsk: Fs::zero(),
        bvk: edwards::Point::zero(),
    });

    Box::into_raw(ctx)
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_proving_ctx_free(ctx: *mut SaplingProvingContext) {
    drop(unsafe { Box::from_raw(ctx) });
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_verification_ctx_init(
) -> *mut SaplingVerificationContext {
    let ctx = Box::new(SaplingVerificationContext {
        bvk: edwards::Point::zero(),
    });

    Box::into_raw(ctx)
}

#[no_mangle]
pub extern "system" fn librustzcash_sapling_verification_ctx_free(
    ctx: *mut SaplingVerificationContext,
) {
    drop(unsafe { Box::from_raw(ctx) });
}

#[no_mangle]
pub unsafe extern "C" fn Java_work_samosudov_rustlib_RustAPI_initWallet(
    env: JNIEnv<'_>,
    _: JClass<'_>,
    seed: jbyteArray,
) {

    let seed = env.convert_byte_array(seed).unwrap();

    let xsk = ExtendedSpendingKey::master(&seed);
}

#[no_mangle]
pub unsafe extern "C" fn Java_work_samosudov_rustlib_RustAPI_dPart(
    env: JNIEnv<'_>,
    _: JClass<'_>,
    seed: jbyteArray,
) -> jbyteArray {

    let seed = env.convert_byte_array(seed).unwrap();

    let extsk = spending_key(&seed, 1, 0);

    let extfvk = ExtendedFullViewingKey::from(&extsk);

    let expsk = &extsk.expsk;

    let ask = expsk.ask;//32
    let nsk = expsk.nsk;//32
    let ovk = extfvk.fvk.ovk;

    let fvk = &extfvk.fvk;
    let ak = &fvk.vk.ak;//32
    let nk = &fvk.vk.nk;//32
    let ivk = &fvk.vk.ivk();

    let addr = extfvk.default_address().unwrap().1;

    let d = addr.diversifier.0;
    let pkd = addr.pk_d.clone();

    let mut res = vec![];
    let mut buf = [0; 32];
    //ask
    ask.into_repr().write_le(&mut buf[..]).unwrap();
    res.extend_from_slice(&buf);
    //nsk
    nsk.into_repr().write_le(&mut buf[..]).unwrap();
    res.extend_from_slice(&buf);
    //ovk
    res.extend_from_slice(&ovk.0);
    //ak
    ak.write(&mut buf[..]).unwrap();
    res.extend_from_slice(&buf);
    //nk
    nk.write(&mut buf[..]).unwrap();
    res.extend_from_slice(&buf);
    //ivk
    ivk.into_repr().write_le(&mut buf[..]).unwrap();
    res.extend_from_slice(&buf);
    //d
    res.extend_from_slice(&d);
    //pkd
    pkd.write(&mut buf[..]).unwrap();
    res.extend_from_slice(&buf);

    env.byte_array_from_slice(res.as_slice()).expect("Could not convert u8 vec into java byte array!")
}

#[no_mangle]
pub unsafe extern "C" fn Java_work_samosudov_rustlib_RustAPI_getExtsk(
    env: JNIEnv<'_>,
    _: JClass<'_>,
    seed: jbyteArray,
) -> jstring {

    let seed = env.convert_byte_array(seed).unwrap();

    let extsk = spending_key(&seed, 1, 0);


    let spending_key = encode_extended_spending_key(HRP_SAPLING_EXTENDED_SPENDING_KEY_MAIN,
    //let spending_key = encode_extended_spending_key(HRP_SAPLING_EXTENDED_SPENDING_KEY_TEST,
                                                  &extsk,
                                                  );

    let output = env.new_string(spending_key)
            .expect("Couldn't create java string!");

    output.into_inner()
}

#[no_mangle]
pub unsafe extern "C" fn Java_work_samosudov_rustlib_RustAPI_compactDecrypt(
    env: JNIEnv<'_>,
    _: JClass<'_>,
    key: jbyteArray,
    cypher: jbyteArray,
) -> jbyteArray {

    let key = env.convert_byte_array(key).unwrap();
    let cypher = env.convert_byte_array(cypher).unwrap();

    // Prefix plaintext with 64 zero-bytes to skip over Poly1305 keying output
    const CHACHA20_BLOCK_SIZE: usize = 64;
    let mut plaintext = [0; CHACHA20_BLOCK_SIZE + COMPACT_NOTE_SIZE];
    plaintext[CHACHA20_BLOCK_SIZE..].copy_from_slice(&cypher[0..COMPACT_NOTE_SIZE]);
    let cha = ChaCha20Ietf::cipher()
                .decrypt(
                    &mut plaintext,
                    CHACHA20_BLOCK_SIZE + COMPACT_NOTE_SIZE,
                    &key,
                    &[0u8; 12],
                );

    let mut res = vec![];
    if cha.is_ok() {
        res.extend_from_slice(&plaintext[CHACHA20_BLOCK_SIZE..]);
    } else {
        let mut buf = [0; 0];
        res.extend_from_slice(&buf);
    }

    env.byte_array_from_slice(res.as_slice()).expect("Could not convert u8 vec into java byte array!")
}

#[no_mangle]
pub unsafe extern "C" fn Java_work_samosudov_rustlib_RustAPI_encryptNp(
    env: JNIEnv<'_>,
    _: JClass<'_>,
    key: jbyteArray,
    cypher: jbyteArray,
) -> jbyteArray {

            let key = env.convert_byte_array(key).unwrap();
            let cypher = env.convert_byte_array(cypher).unwrap();

            assert_eq!(cypher.len(), NOTE_PLAINTEXT_SIZE);

            let mut output = [0u8; ENC_CIPHERTEXT_SIZE];
            assert_eq!(
                ChachaPolyIetf::aead_cipher()
                    .seal_to(&mut output, &cypher, &[], &key, &[0u8; 12])
                    .unwrap(),
                ENC_CIPHERTEXT_SIZE
            );

            env.byte_array_from_slice(&output).expect("Could not convert u8 vec into java byte array!")
}

pub fn spending_key(seed: &[u8], coin_type: u32, account: u32) -> ExtendedSpendingKey {
    ExtendedSpendingKey::from_path(
        &ExtendedSpendingKey::master(&seed),
        &[
            ChildIndex::Hardened(32),
            ChildIndex::Hardened(coin_type),
            ChildIndex::Hardened(account),
        ],
    )
}

#[no_mangle]
pub unsafe extern "C" fn Java_work_samosudov_rustlib_RustAPI_zAddrFromWif(
    env: JNIEnv<'_>,
    _: JClass<'_>,
    seed: jbyteArray,
) -> jstring {

    let seed = env.convert_byte_array(seed).unwrap();

    let extsk = spending_key(&seed, 1, 0);

    let extfvk = ExtendedFullViewingKey::from(&extsk);

    let address = address_from_extfvk(&extfvk);

    let output = env.new_string(address)
            .expect("Couldn't create java string!");

    output.into_inner()
}

fn address_from_extfvk(extfvk: &ExtendedFullViewingKey) -> String {
    let addr = extfvk.default_address().unwrap().1;
    encode_payment_address(HRP_SAPLING_PAYMENT_ADDRESS_MAIN, &addr)
    //encode_payment_address(HRP_SAPLING_PAYMENT_ADDRESS_TEST, &addr)
}
