#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <assert.h>

typedef uint64_t u64;

#ifndef ZAPLING_LIB
#define ZAPLING_LIB

#ifdef __cplusplus
extern "C" {
#endif

void librustzcash_sapling_generate_r(
        unsigned char *result
);

void librustzcash_init_zksnark_params(
        const char *output_bytes,
        const char* output_hash,
        const char *spend_bytes,
        const char* spend_hash
);

bool librustzcash_sapling_ka_derivepublic(
        const unsigned char *diversifier,
        const unsigned char *esk,
        unsigned char *result
);

bool librustzcash_sapling_compute_cm(
        const unsigned char *diversifier,
        const unsigned char *pk_d,
        const uint64_t value,
        const unsigned char *r,
        unsigned char *result
);

bool librustzcash_sapling_output_proof(
        void *ctx,
        const unsigned char *esk,
        const unsigned char *diversifier,
        const unsigned char *pk_d,
        const unsigned char *rcm,
        const uint64_t value,
        unsigned char *cv,
        unsigned char *zkproof
);

bool librustzcash_sapling_binding_sig(
        const void *ctx,
        int64_t valueBalance,
        const unsigned char *sighash,
        unsigned char *result
);

bool librustzcash_sapling_ka_agree(
        const unsigned char *p,
        const unsigned char *sk,
        unsigned char *result
);

bool librustzcash_ivk_to_pkd(
        const unsigned char *ivk,
        const unsigned char *diversifier,
        unsigned char *result);

uint32_t librustzcash_sapling_check_output(
        void *ctx,
        const unsigned char *cv,
        const unsigned char *cm,
        const unsigned char *ephemeralKey,
        const unsigned char *zkproof
);

void librustzcash_merkle_hash(
        size_t depth,
        const unsigned char *a,
        const unsigned char *b,
        unsigned char *result
);

void librustzcash_tree_uncommitted(
        unsigned char *result
);

bool librustzcash_sapling_compute_nf(
        const unsigned char *diversifier,
        const unsigned char *pk_d,
        const uint64_t value,
        const unsigned char *r,
        const unsigned char *ak,
        const unsigned char *nk,
        const uint64_t position,
        unsigned char *result
);

bool librustzcash_sapling_spend_proof(
        void *ctx,
        const unsigned char *ak,
        const unsigned char *nsk,
        const unsigned char *diversifier,
        const unsigned char *rcm,
        const unsigned char *ar,
        const uint64_t value,
        const unsigned char *anchor,
        const unsigned char *witness,
        unsigned char *cv,
        unsigned char *rk,
        unsigned char *zkproof,
        unsigned char *nf
);

unsigned char* encrypt_note_plaintext(
        const unsigned char *key,
        const unsigned char *encCiphertext,
        unsigned char *result
);

bool librustzcash_sapling_spend_sig(
        const unsigned char *ask,
        const unsigned char *ar,
        const unsigned char *sighash,
        unsigned char *result
);

void librustzcash_test_verify();

bool librustzcash_sapling_check_spend(
        void *ctx,
        const unsigned char *cv,
        const unsigned char *anchor,
        const unsigned char *nullifier,
        const unsigned char *rk,
        const unsigned char *zkproof,
        const unsigned char *spendAuthSig,
        const unsigned char *sighashValue
);

bool librustzcash_sapling_final_check(
        void *ctx,
        int64_t valueBalance,
        const unsigned char *bindingSig,
        const unsigned char *sighashValue
);


void * librustzcash_sapling_proving_ctx_init();

void librustzcash_sapling_proving_ctx_free(void *);

void * librustzcash_sapling_verification_ctx_init();

void librustzcash_sapling_verification_ctx_free(void *);

#ifdef __cplusplus
}
#endif

#endif /* ZAPLING_LIB */