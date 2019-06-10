use ff::{PrimeField, PrimeFieldRepr};
use pairing::bls12_381::{Bls12, Fr, FrRepr};
use protobuf::parse_from_bytes;
use sapling_crypto::jubjub::{edwards, fs::Fs};
use std::collections::HashSet;
use zcash_primitives::{
    merkle_tree::{CommitmentTree, IncrementalWitness, Node},
    note_encryption::try_sapling_compact_note_decryption,
    transaction::TxId,
    JUBJUB,
};
use zip32::ExtendedFullViewingKey;

use crate::proto::compact_formats::{CompactBlock, CompactOutput};
use crate::wallet::{WalletShieldedOutput, WalletShieldedSpend, WalletTx};

/// Returns a WalletShieldedOutput and corresponding IncrementalWitness if this
/// output belongs to any of the given ExtendedFullViewingKeys. The given
/// CommitmentTree and existing IncrementalWitnesses are incremented with this
/// output's commitment.
fn scan_output(
    (index, output): (usize, CompactOutput),
    ivks: &[Fs],
    spent_from_accounts: &HashSet<usize>,
    tree: &mut CommitmentTree,
    existing_witnesses: &mut [&mut IncrementalWitness],
    block_witnesses: &mut [&mut IncrementalWitness],
    new_witnesses: &mut [IncrementalWitness],
) -> Option<(WalletShieldedOutput, IncrementalWitness)> {
    let mut repr = FrRepr::default();
    if repr.read_le(&output.cmu[..]).is_err() {
        return None;
    }
    let cmu = match Fr::from_repr(repr) {
        Ok(cmu) => cmu,
        Err(_) => return None,
    };

    let epk = match edwards::Point::<Bls12, _>::read(&output.epk[..], &JUBJUB) {
        Ok(p) => match p.as_prime_order(&JUBJUB) {
            Some(epk) => epk,
            None => return None,
        },
        Err(_) => return None,
    };

    let ct = output.ciphertext;

    // Increment tree and witnesses
    let node = Node::new(cmu.into_repr());
    for witness in existing_witnesses {
        witness.append(node).unwrap();
    }
    for witness in block_witnesses {
        witness.append(node).unwrap();
    }
    for witness in new_witnesses {
        witness.append(node).unwrap();
    }
    tree.append(node).unwrap();

    for (account, ivk) in ivks.iter().enumerate() {
        let (note, to) = match try_sapling_compact_note_decryption(ivk, &epk, &cmu, &ct) {
            Some(ret) => ret,
            None => continue,
        };

        // A note is marked as "change" if the account that received it
        // also spent notes in the same transaction. This will catch,
        // for instance:
        // - Change created by spending fractions of notes.
        // - Notes created by consolidation transactions.
        // - Notes sent from one account to itself.
        let is_change = spent_from_accounts.contains(&account);

        return Some((
            WalletShieldedOutput {
                index,
                cmu,
                epk,
                account,
                note,
                to,
                is_change,
            },
            IncrementalWitness::from_tree(tree),
        ));
    }
    None
}

/// Returns a vector of transactions belonging to any of the given
/// ExtendedFullViewingKeys, and the corresponding new IncrementalWitnesses.
/// The given CommitmentTree and existing IncrementalWitnesses are
/// incremented appropriately.
pub fn scan_block(
    block: CompactBlock,
    extfvks: &[ExtendedFullViewingKey],
    nullifiers: &[(&[u8], usize)],
    tree: &mut CommitmentTree,
    existing_witnesses: &mut [&mut IncrementalWitness],
) -> Vec<(WalletTx, Vec<IncrementalWitness>)> {
    let mut wtxs: Vec<(WalletTx, Vec<IncrementalWitness>)> = vec![];
    let ivks: Vec<_> = extfvks.iter().map(|extfvk| extfvk.fvk.vk.ivk()).collect();

    for tx in block.vtx.into_iter() {
        let num_spends = tx.spends.len();
        let num_outputs = tx.outputs.len();

        // Check for spent notes
        let shielded_spends: Vec<_> =
            tx.spends
                .into_iter()
                .enumerate()
                .filter_map(|(index, spend)| {
                    if let Some(account) = nullifiers.iter().find_map(|&(nf, acc)| {
                        if nf == &spend.nf[..] {
                            Some(acc)
                        } else {
                            None
                        }
                    }) {
                        Some(WalletShieldedSpend {
                            index,
                            nf: spend.nf,
                            account,
                        })
                    } else {
                        None
                    }
                })
                .collect();

        // Collect the set of accounts that were spent from in this transaction
        let spent_from_accounts: HashSet<_> =
            shielded_spends.iter().map(|spend| spend.account).collect();

        // Check for incoming notes while incrementing tree and witnesses
        let mut shielded_outputs = vec![];
        let mut new_witnesses = vec![];
        {
            // Grab mutable references to new witnesses from previous transactions
            // in this block so that we can update them. Scoped so we don't hold
            // mutable references to wtxs for too long.
            let mut block_witnesses: Vec<_> = wtxs
                .iter_mut()
                .map(|(_, w)| w.iter_mut().collect::<Vec<_>>())
                .flatten()
                .collect();

            for to_scan in tx.outputs.into_iter().enumerate() {
                if let Some((output, new_witness)) = scan_output(
                    to_scan,
                    &ivks,
                    &spent_from_accounts,
                    tree,
                    existing_witnesses,
                    &mut block_witnesses,
                    &mut new_witnesses,
                ) {
                    shielded_outputs.push(output);
                    new_witnesses.push(new_witness);
                }
            }
        }

        if !(shielded_spends.is_empty() && shielded_outputs.is_empty()) {
            let mut txid = TxId([0u8; 32]);
            txid.0.copy_from_slice(&tx.hash);
            wtxs.push((
                WalletTx {
                    txid,
                    index: tx.index as usize,
                    num_spends,
                    num_outputs,
                    shielded_spends,
                    shielded_outputs,
                },
                new_witnesses,
            ));
        }
    }

    wtxs
}

/// Returns a vector of transactions belonging to any of the given
/// ExtendedFullViewingKeys, and the corresponding new IncrementalWitnesses.
/// The given CommitmentTree and existing IncrementalWitnesses are
/// incremented appropriately.
pub fn scan_block_from_bytes(
    block: &[u8],
    extfvks: &[ExtendedFullViewingKey],
    nullifiers: &[(&[u8], usize)],
    tree: &mut CommitmentTree,
    witnesses: &mut [&mut IncrementalWitness],
) -> Vec<(WalletTx, Vec<IncrementalWitness>)> {
    let block: CompactBlock =
        parse_from_bytes(block).expect("Cannot convert into a `CompactBlock`");

    scan_block(block, extfvks, nullifiers, tree, witnesses)
}

#[cfg(test)]
mod tests {
    use ff::{PrimeField, PrimeFieldRepr};
    use pairing::bls12_381::{Bls12, Fr};
    use rand::{thread_rng, Rand, Rng};
    use sapling_crypto::{
        jubjub::{fs::Fs, FixedGenerators, JubjubParams, ToUniform},
        primitives::Note,
    };
    use zcash_primitives::{
        merkle_tree::CommitmentTree,
        note_encryption::{Memo, SaplingNoteEncryption},
        transaction::components::Amount,
        JUBJUB,
    };
    use zip32::{ExtendedFullViewingKey, ExtendedSpendingKey};

    use super::scan_block;
    use crate::proto::compact_formats::{CompactBlock, CompactOutput, CompactTx};

    fn random_compact_tx<R: Rng>(rng: &mut R) -> CompactTx {
        let fake_cmu = {
            let fake_cmu = Fr::rand(rng);
            let mut bytes = vec![];
            fake_cmu.into_repr().write_le(&mut bytes).unwrap();
            bytes
        };
        let fake_epk = {
            let mut buffer = vec![0; 64];
            rng.fill_bytes(&mut buffer);
            let fake_esk = Fs::to_uniform(&buffer[..]);
            let fake_epk = JUBJUB
                .generator(FixedGenerators::SpendingKeyGenerator)
                .mul(fake_esk, &JUBJUB);
            let mut bytes = vec![];
            fake_epk.write(&mut bytes).unwrap();
            bytes
        };
        let mut cout = CompactOutput::new();
        cout.set_cmu(fake_cmu);
        cout.set_epk(fake_epk);
        cout.set_ciphertext(vec![0; 52]);
        let mut ctx = CompactTx::new();
        let mut txid = vec![0; 32];
        rng.fill_bytes(&mut txid);
        ctx.set_hash(txid);
        ctx.outputs.push(cout);
        ctx
    }

    /// Create a fake CompactBlock at the given height, containing a single output paying
    /// the given address. Returns the CompactBlock and the nullifier for the new note.
    fn fake_compact_block(
        height: i32,
        extfvk: ExtendedFullViewingKey,
        value: Amount,
        tx_after: bool,
    ) -> CompactBlock {
        let to = extfvk.default_address().unwrap().1;

        // Create a fake Note for the account
        let mut rng = thread_rng();
        let note = Note {
            g_d: to.diversifier.g_d::<Bls12>(&JUBJUB).unwrap(),
            pk_d: to.pk_d.clone(),
            value: value.0 as u64,
            r: Fs::rand(&mut rng),
        };
        let encryptor =
            SaplingNoteEncryption::new(extfvk.fvk.ovk, note.clone(), to.clone(), Memo::default());
        let mut cmu = vec![];
        note.cm(&JUBJUB).into_repr().write_le(&mut cmu).unwrap();
        let mut epk = vec![];
        encryptor.epk().write(&mut epk).unwrap();
        let enc_ciphertext = encryptor.encrypt_note_plaintext();

        // Create a fake CompactBlock containing the note
        let mut cb = CompactBlock::new();
        cb.set_height(height as u64);

        // Add a random Sapling tx before ours
        cb.vtx.push(random_compact_tx(&mut rng));

        let mut cout = CompactOutput::new();
        cout.set_cmu(cmu);
        cout.set_epk(epk);
        cout.set_ciphertext(enc_ciphertext[..52].to_vec());
        let mut ctx = CompactTx::new();
        let mut txid = vec![0; 32];
        rng.fill_bytes(&mut txid);
        ctx.set_hash(txid);
        ctx.outputs.push(cout);
        cb.vtx.push(ctx);

        // Optionally add another random Sapling tx after ours
        if tx_after {
            cb.vtx.push(random_compact_tx(&mut rng));
        }

        cb
    }

    #[test]
    fn scan_block_with_my_tx() {
        let extsk = ExtendedSpendingKey::master(&[]);
        let extfvk = ExtendedFullViewingKey::from(&extsk);

        let cb = fake_compact_block(1, extfvk.clone(), Amount(5), false);

        let mut tree = CommitmentTree::new();
        let txs = scan_block(cb, &[extfvk], &[], &mut tree, &mut []);

        // Check after each output that the roots all match
        for (_, new_witnesses) in txs {
            for witness in new_witnesses {
                assert_eq!(witness.root(), tree.root());
            }
        }
    }

    #[test]
    fn scan_block_with_txs_after_my_tx() {
        let extsk = ExtendedSpendingKey::master(&[]);
        let extfvk = ExtendedFullViewingKey::from(&extsk);

        let cb = fake_compact_block(1, extfvk.clone(), Amount(5), true);

        let mut tree = CommitmentTree::new();
        let txs = scan_block(cb, &[extfvk], &[], &mut tree, &mut []);

        // Check after each output that the roots all match
        for (_, new_witnesses) in txs {
            for witness in new_witnesses {
                assert_eq!(witness.root(), tree.root());
            }
        }
    }
}
