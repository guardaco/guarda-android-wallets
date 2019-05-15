use zip32::{ChildIndex, ExtendedSpendingKey};

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
