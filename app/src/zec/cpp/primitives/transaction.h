// Copyright (c) 2009-2010 Satoshi Nakamoto
// Copyright (c) 2009-2014 The Bitcoin Core developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

#ifndef BITCOIN_PRIMITIVES_TRANSACTION_H
#define BITCOIN_PRIMITIVES_TRANSACTION_H

static constexpr size_t GROTH_PROOF_SIZE = (
        48 + // π_A
        96 + // π_B
        48); // π_C

typedef std::array<unsigned char, GROTH_PROOF_SIZE> GrothProof;

/**
 * A shielded input to a transaction. It contains data that describes a Spend transfer.
 */
class SpendDescription
{
public:
    typedef std::array<unsigned char, 64> spend_auth_sig_t;

    uint256 cv;                    //!< A value commitment to the value of the input note.
    uint256 anchor;                //!< A Merkle root of the Sapling note commitment tree at some block height in the past.
    uint256 nullifier;             //!< The nullifier of the input note.
    uint256 rk;                    //!< The randomized public key for spendAuthSig.
    GrothProof zkproof;  //!< A zero-knowledge proof using the spend circuit.
    spend_auth_sig_t spendAuthSig; //!< A signature authorizing this spend.

    SpendDescription() { }

    ADD_SERIALIZE_METHODS;

    template <typename Stream, typename Operation>
    inline void SerializationOp(Stream& s, Operation ser_action) {
        READWRITE(cv);
        READWRITE(anchor);
        READWRITE(nullifier);
        READWRITE(rk);
        READWRITE(zkproof);
        READWRITE(spendAuthSig);
    }

    friend bool operator==(const SpendDescription& a, const SpendDescription& b)
    {
        return (
                a.cv == b.cv &&
                a.anchor == b.anchor &&
                a.nullifier == b.nullifier &&
                a.rk == b.rk &&
                a.zkproof == b.zkproof &&
                a.spendAuthSig == b.spendAuthSig
        );
    }

    friend bool operator!=(const SpendDescription& a, const SpendDescription& b)
    {
        return !(a == b);
    }
};

#endif // BITCOIN_PRIMITIVES_TRANSACTION_H
