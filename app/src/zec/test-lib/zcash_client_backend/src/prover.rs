use bellman::groth16::{Parameters, PreparedVerifyingKey};
use pairing::bls12_381::{Bls12, Fr};
use sapling_crypto::{
    jubjub::{edwards, fs::Fs, Unknown},
    primitives::{Diversifier, PaymentAddress, ProofGenerationKey},
    redjubjub::PublicKey,
};
use std::path::Path;
use zcash_primitives::{
    merkle_tree::CommitmentTreeWitness, transaction::components::GROTH_PROOF_SIZE, JUBJUB,
};
use zcash_proofs::{load_parameters, sapling::SaplingProvingContext};

/// Interface for creating zero-knowledge proofs for shielded transactions.
pub trait TxProver {
    /// Create the value commitment, re-randomized key, and proof for a Sapling
    /// SpendDescription, while accumulating its value commitment randomness
    /// inside the context for later use.
    fn spend_proof(
        &self,
        ctx: &mut SaplingProvingContext,
        proof_generation_key: ProofGenerationKey<Bls12>,
        diversifier: Diversifier,
        rcm: Fs,
        ar: Fs,
        value: u64,
        anchor: Fr,
        witness: CommitmentTreeWitness,
    ) -> Result<
        (
            [u8; GROTH_PROOF_SIZE],
            edwards::Point<Bls12, Unknown>,
            PublicKey<Bls12>,
        ),
        (),
    >;

    /// Create the value commitment and proof for a Sapling OutputDescription,
    /// while accumulating its value commitment randomness inside the context
    /// for later use.
    fn output_proof(
        &self,
        ctx: &mut SaplingProvingContext,
        esk: Fs,
        payment_address: PaymentAddress<Bls12>,
        rcm: Fs,
        value: u64,
    ) -> ([u8; GROTH_PROOF_SIZE], edwards::Point<Bls12, Unknown>);
}

pub struct LocalTxProver {
    spend_params: Parameters<Bls12>,
    spend_vk: PreparedVerifyingKey<Bls12>,
    output_params: Parameters<Bls12>,
}

impl LocalTxProver {
    pub fn new(spend_path: &Path, spend_hash: &str, output_path: &Path, output_hash: &str) -> Self {
        let (spend_params, spend_vk, output_params, _, _) =
            load_parameters(spend_path, spend_hash, output_path, output_hash, None, None);
        LocalTxProver {
            spend_params,
            spend_vk,
            output_params,
        }
    }
}

impl TxProver for LocalTxProver {
    fn spend_proof(
        &self,
        ctx: &mut SaplingProvingContext,
        proof_generation_key: ProofGenerationKey<Bls12>,
        diversifier: Diversifier,
        rcm: Fs,
        ar: Fs,
        value: u64,
        anchor: Fr,
        witness: CommitmentTreeWitness,
    ) -> Result<
        (
            [u8; GROTH_PROOF_SIZE],
            edwards::Point<Bls12, Unknown>,
            PublicKey<Bls12>,
        ),
        (),
    > {
        let (proof, cv, rk, nf) = ctx.spend_proof(
            proof_generation_key,
            diversifier,
            rcm,
            ar,
            value,
            anchor,
            witness,
            &self.spend_params,
            &self.spend_vk,
            &JUBJUB,
        )?;

        let mut zkproof = [0u8; GROTH_PROOF_SIZE];
        proof
            .write(&mut zkproof[..])
            .expect("should be able to serialize a proof");

        Ok((zkproof, cv, rk))
    }

    fn output_proof(
        &self,
        ctx: &mut SaplingProvingContext,
        esk: Fs,
        payment_address: PaymentAddress<Bls12>,
        rcm: Fs,
        value: u64,
    ) -> ([u8; GROTH_PROOF_SIZE], edwards::Point<Bls12, Unknown>) {
        let (proof, cv) = ctx.output_proof(
            esk,
            payment_address,
            rcm,
            value,
            &self.output_params,
            &JUBJUB,
        );

        let mut zkproof = [0u8; GROTH_PROOF_SIZE];
        proof
            .write(&mut zkproof[..])
            .expect("should be able to serialize a proof");

        (zkproof, cv)
    }
}

#[cfg(test)]
pub(crate) mod mock {
    use pairing::bls12_381::{Bls12, Fr};
    use rand::{OsRng, Rand};
    use sapling_crypto::{
        jubjub::{edwards, fs::Fs, FixedGenerators, Unknown},
        primitives::{Diversifier, PaymentAddress, ProofGenerationKey, ValueCommitment},
        redjubjub::PublicKey,
    };
    use zcash_primitives::{
        merkle_tree::CommitmentTreeWitness, transaction::components::GROTH_PROOF_SIZE, JUBJUB,
    };
    use zcash_proofs::sapling::SaplingProvingContext;

    use super::TxProver;

    pub(crate) struct MockTxProver;

    #[cfg(test)]
    impl TxProver for MockTxProver {
        fn spend_proof(
            &self,
            _ctx: &mut SaplingProvingContext,
            proof_generation_key: ProofGenerationKey<Bls12>,
            _diversifier: Diversifier,
            _rcm: Fs,
            ar: Fs,
            value: u64,
            _anchor: Fr,
            _witness: CommitmentTreeWitness,
        ) -> Result<
            (
                [u8; GROTH_PROOF_SIZE],
                edwards::Point<Bls12, Unknown>,
                PublicKey<Bls12>,
            ),
            (),
        > {
            let mut rng = OsRng::new().expect("should be able to construct RNG");

            let cv = ValueCommitment::<Bls12> {
                value,
                randomness: Fs::rand(&mut rng),
            }
            .cm(&JUBJUB)
            .into();

            let rk = PublicKey::<Bls12>(proof_generation_key.ak.clone().into()).randomize(
                ar,
                FixedGenerators::SpendingKeyGenerator,
                &JUBJUB,
            );

            Ok(([0u8; GROTH_PROOF_SIZE], cv, rk))
        }

        fn output_proof(
            &self,
            _ctx: &mut SaplingProvingContext,
            _esk: Fs,
            _payment_address: PaymentAddress<Bls12>,
            _rcm: Fs,
            value: u64,
        ) -> ([u8; GROTH_PROOF_SIZE], edwards::Point<Bls12, Unknown>) {
            let mut rng = OsRng::new().expect("should be able to construct RNG");

            let cv = ValueCommitment::<Bls12> {
                value,
                randomness: Fs::rand(&mut rng),
            }
            .cm(&JUBJUB)
            .into();

            ([0u8; GROTH_PROOF_SIZE], cv)
        }
    }
}
