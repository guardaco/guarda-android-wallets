use pairing::bls12_381::{Bls12, Fr};
use sapling_crypto::{
    jubjub::{edwards, PrimeOrder},
    primitives::{Note, PaymentAddress},
};
use zcash_primitives::transaction::TxId;

pub struct WalletTx {
    pub txid: TxId,
    pub index: usize,
    pub num_spends: usize,
    pub num_outputs: usize,
    pub shielded_spends: Vec<WalletShieldedSpend>,
    pub shielded_outputs: Vec<WalletShieldedOutput>,
}

pub struct WalletShieldedSpend {
    pub index: usize,
    pub nf: Vec<u8>,
    pub account: usize,
}

pub struct WalletShieldedOutput {
    pub index: usize,
    pub cmu: Fr,
    pub epk: edwards::Point<Bls12, PrimeOrder>,
    pub account: usize,
    pub note: Note<Bls12>,
    pub to: PaymentAddress<Bls12>,
    pub is_change: bool,
}
