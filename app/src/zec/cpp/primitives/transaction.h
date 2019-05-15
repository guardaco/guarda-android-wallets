// Copyright (c) 2009-2010 Satoshi Nakamoto
// Copyright (c) 2009-2014 The Bitcoin Core developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

#ifndef BITCOIN_PRIMITIVES_TRANSACTION_H
#define BITCOIN_PRIMITIVES_TRANSACTION_H

//#include "amount.h"
//#include "random.h"
//#include "script/script.h"
//#include "serialize.h"
//#include "streams.h"
//#include "uint256.h"
//#include "consensus/consensus.h"
//
//#include <array>
//
//#include <boost/variant.hpp>
//
//#include "zcash/NoteEncryption.hpp"
//#include "zcash/Zcash.h"
//#include "zcash/JoinSplit.hpp"
//#include "zcash/Proof.hpp"

// Overwinter transaction version
//static const int32_t OVERWINTER_TX_VERSION = 3;
//static_assert(OVERWINTER_TX_VERSION >= OVERWINTER_MIN_TX_VERSION,
//    "Overwinter tx version must not be lower than minimum");
//static_assert(OVERWINTER_TX_VERSION <= OVERWINTER_MAX_TX_VERSION,
//    "Overwinter tx version must not be higher than maximum");
//
//// Sapling transaction version
//static const int32_t SAPLING_TX_VERSION = 4;
//static_assert(SAPLING_TX_VERSION >= SAPLING_MIN_TX_VERSION,
//    "Sapling tx version must not be lower than minimum");
//static_assert(SAPLING_TX_VERSION <= SAPLING_MAX_TX_VERSION,
//    "Sapling tx version must not be higher than maximum");

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
