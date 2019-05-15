use hex;
use std::fmt;

#[derive(Debug, PartialEq, Eq, Hash)]
pub struct Nullifier(pub [u8; 32]);

impl fmt::Display for Nullifier {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        let mut data = self.0.to_vec();
        data.reverse();
        formatter.write_str(&hex::encode(data))
    }
}

pub struct EncCiphertextFrag(pub [u8; 52]);

impl fmt::Display for EncCiphertextFrag {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter.write_str(&hex::encode(&self.0[..]))
    }
}
