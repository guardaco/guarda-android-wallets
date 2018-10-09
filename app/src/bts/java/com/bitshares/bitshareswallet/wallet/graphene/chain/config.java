package com.bitshares.bitshareswallet.wallet.graphene.chain;



public class config {
    //public static final String GRAPHENE_SYMBOL               = "TEST";
    //public static final String GRAPHENE_ADDRESS_PREFIX       = "TEST";
    public static final String GRAPHENE_SYMBOL               = "BTS";
    public static final String GRAPHENE_ADDRESS_PREFIX       = "BTS";
    public static final int GRAPHENE_MIN_ACCOUNT_NAME_LENGTH = 1;
    public static final int GRAPHENE_MAX_ACCOUNT_NAME_LENGTH = 63;

    public static final int GRAPHENE_MIN_ASSET_SYMBOL_LENGTH = 3;
    public static final int GRAPHENE_MAX_ASSET_SYMBOL_LENGTH = 16;

    public static final long GRAPHENE_MAX_SHARE_SUPPLY       = 1000000000000000l;

    public static final int GRAPHENE_MAX_PAY_RATE            = 10000; /* 100% */
    public static final int GRAPHENE_MAX_SIG_CHECK_DEPTH     = 2;

/**
 * Don't allow the committee_members to publish a limit that would
 * make the network unable to operate.
 */
    public static final int GRAPHENE_MIN_TRANSACTION_SIZE_LIMIT = 1024;
    public static final int GRAPHENE_MIN_BLOCK_INTERVAL         = 1; /* seconds */
    public static final int GRAPHENE_MAX_BLOCK_INTERVAL         = 30; /* seconds */

    public static final int GRAPHENE_DEFAULT_BLOCK_INTERVAL     = 5; /* seconds */
    public static final int GRAPHENE_DEFAULT_MAX_TRANSACTION_SIZE  = 2048;
    public static final int GRAPHENE_DEFAULT_MAX_BLOCK_SIZE     = (GRAPHENE_DEFAULT_MAX_TRANSACTION_SIZE*GRAPHENE_DEFAULT_BLOCK_INTERVAL*200000);
    public static final int GRAPHENE_DEFAULT_MAX_TIME_UNTIL_EXPIRATION = (60*60*24); // seconds,  aka: 1 day
    public static final int GRAPHENE_DEFAULT_MAINTENANCE_INTERVAL  = (60*60*24); // seconds, aka: 1 day
    public static final int GRAPHENE_DEFAULT_MAINTENANCE_SKIP_SLOTS = 3;  // number of slots to skip for maintenance interval

    public static final int GRAPHENE_MIN_UNDO_HISTORY = 10;
    public static final int GRAPHENE_MAX_UNDO_HISTORY = 10000;

    public static final int GRAPHENE_MIN_BLOCK_SIZE_LIMIT = (GRAPHENE_MIN_TRANSACTION_SIZE_LIMIT*5); // 5 transactions per block
    public static final int GRAPHENE_MIN_TRANSACTION_EXPIRATION_LIMIT = (GRAPHENE_MAX_BLOCK_INTERVAL * 5); // 5 transactions per block
    public static final int GRAPHENE_BLOCKCHAIN_PRECISION       =        100000;

    public static final int GRAPHENE_BLOCKCHAIN_PRECISION_DIGITS = 5;
    public static final long GRAPHENE_DEFAULT_TRANSFER_FEE       = (1*GRAPHENE_BLOCKCHAIN_PRECISION);

    public static final long GRAPHENE_MAX_INSTANCE_ID            = (-1 >> 16); // (uint64_t(-1)>>16)

    /** percentage fields are fixed point with a denominator of 10,000 */
    public static final int GRAPHENE_100_PERCENT                 = 10000;
    public static final int GRAPHENE_1_PERCENT                   = (GRAPHENE_100_PERCENT / 100);

    /** NOTE: making this a power of 2 (say 2^15) would greatly accelerate fee calcs */
    public static final int GRAPHENE_MAX_MARKET_FEE_PERCENT      = GRAPHENE_100_PERCENT;

    public static final int GRAPHENE_DEFAULT_FORCE_SETTLEMENT_DELAY  = (60*60*24); ///< 1 day
    public static final int GRAPHENE_DEFAULT_FORCE_SETTLEMENT_OFFSET = 0; ///< 1%
    public static final int GRAPHENE_DEFAULT_FORCE_SETTLEMENT_MAX_VOLUME = (20* GRAPHENE_1_PERCENT); ///< 20%
    public static final int GRAPHENE_DEFAULT_PRICE_FEED_LIFETIME         = (60*60*24); ///< 1 day
    public static final int GRAPHENE_MAX_FEED_PRODUCERS                  = 200;
    public static final int GRAPHENE_DEFAULT_MAX_AUTHORITY_MEMBERSHIP              = 10;
    public static final int GRAPHENE_DEFAULT_MAX_ASSET_WHITELIST_AUTHORITIES       = 10;
    public static final int GRAPHENE_DEFAULT_MAX_ASSET_FEED_PUBLISHERS             = 10;

    /**
     *  These ratios are fixed point numbers with a denominator of GRAPHENE_COLLATERAL_RATIO_DENOM, the
     *  minimum maitenance collateral is therefore 1.001x and the default
     *  maintenance ratio is 1.75x
     */
    ///@{
    public static final int GRAPHENE_COLLATERAL_RATIO_DENOM                = 1000;
    public static final int GRAPHENE_MIN_COLLATERAL_RATIO                  = 1001;  ///< lower than this could result in divide by 0
    public static final int GRAPHENE_MAX_COLLATERAL_RATIO                  = 32000; ///< higher than this is unnecessary and may exceed int16 storage
    public static final int GRAPHENE_DEFAULT_MAINTENANCE_COLLATERAL_RATIO  = 1200; ///< Call when collateral only pays off 175% the debt
    public static final int GRAPHENE_DEFAULT_MAX_SHORT_SQUEEZE_RATIO       = 1100; ///< Stop calling when collateral only pays off 150% of the debt
    ///@}
    public static final int GRAPHENE_DEFAULT_MARGIN_PERIOD_SEC             = (30*60*60*24);

    public static final int GRAPHENE_DEFAULT_MIN_WITNESS_COUNT                    = (11);
    public static final int GRAPHENE_DEFAULT_MIN_COMMITTEE_MEMBER_COUNT           = (11);
    public static final int GRAPHENE_DEFAULT_MAX_WITNESSES                        = (1001); // SHOULD BE ODD
    public static final int GRAPHENE_DEFAULT_MAX_COMMITTEE                        = (1001); // SHOULD BE ODD
    public static final int GRAPHENE_DEFAULT_MAX_PROPOSAL_LIFETIME_SEC            = (60*60*24*7*4); // Four weeks
    public static final int GRAPHENE_DEFAULT_COMMITTEE_PROPOSAL_REVIEW_PERIOD_SEC = (60*60*24*7*2); // Two weeks
    public static final int GRAPHENE_DEFAULT_NETWORK_PERCENT_OF_FEE               = (20*GRAPHENE_1_PERCENT);
    public static final int GRAPHENE_DEFAULT_LIFETIME_REFERRER_PERCENT_OF_FEE     = (30*GRAPHENE_1_PERCENT);
    public static final int GRAPHENE_DEFAULT_MAX_BULK_DISCOUNT_PERCENT            = (50*GRAPHENE_1_PERCENT);
    public static final long GRAPHENE_DEFAULT_BULK_DISCOUNT_THRESHOLD_MIN         = ( GRAPHENE_BLOCKCHAIN_PRECISION*1000);
    public static final long GRAPHENE_DEFAULT_BULK_DISCOUNT_THRESHOLD_MAX         = ( GRAPHENE_DEFAULT_BULK_DISCOUNT_THRESHOLD_MIN*100);
    public static final int GRAPHENE_DEFAULT_CASHBACK_VESTING_PERIOD_SEC          = (60*60*24*365); ///< 1 year
    public static final long GRAPHENE_DEFAULT_CASHBACK_VESTING_THRESHOLD          = (GRAPHENE_BLOCKCHAIN_PRECISION*100);
    public static final int GRAPHENE_DEFAULT_BURN_PERCENT_OF_FEE                  = (20*GRAPHENE_1_PERCENT);
    public static final int GRAPHENE_WITNESS_PAY_PERCENT_PRECISION                = (1000000000);
    public static final int GRAPHENE_DEFAULT_MAX_ASSERT_OPCODE                    = 1;
    public static final long GRAPHENE_DEFAULT_FEE_LIQUIDATION_THRESHOLD           = GRAPHENE_BLOCKCHAIN_PRECISION * 100;
    public static final int GRAPHENE_DEFAULT_ACCOUNTS_PER_FEE_SCALE               = 1000;
    public static final int GRAPHENE_DEFAULT_ACCOUNT_FEE_SCALE_BITSHIFTS          = 4;
    public static final int GRAPHENE_DEFAULT_MAX_BUYBACK_MARKETS                  = 4;

    public static final int GRAPHENE_MAX_WORKER_NAME_LENGTH                       = 63;

    public static final int GRAPHENE_MAX_URL_LENGTH                               = 127;
/*
// counter initialization values used to derive near and far future seeds for shuffling witnesses
// we use the fractional bits of sqrt(2) in hex
            #define GRAPHENE_NEAR_SCHEDULE_CTR_IV                    ( (uint64_t( 0x6a09 ) << 0x30)    \
            | (uint64_t( 0xe667 ) << 0x20)    \
            | (uint64_t( 0xf3bc ) << 0x10)    \
            | (uint64_t( 0xc908 )        ) )

// and the fractional bits of sqrt(3) in hex
            #define GRAPHENE_FAR_SCHEDULE_CTR_IV                     ( (uint64_t( 0xbb67 ) << 0x30)    \
            | (uint64_t( 0xae85 ) << 0x20)    \
            | (uint64_t( 0x84ca ) << 0x10)    \
            | (uint64_t( 0xa73b )        ) )*/

/**
 * every second, the fraction of burned core asset which cycles is
 * GRAPHENE_CORE_ASSET_CYCLE_RATE / (1 << GRAPHENE_CORE_ASSET_CYCLE_RATE_BITS)
 */
    public static final int GRAPHENE_CORE_ASSET_CYCLE_RATE                    = 17;
    public static final int GRAPHENE_CORE_ASSET_CYCLE_RATE_BITS               = 32;

    public static final long GRAPHENE_DEFAULT_WITNESS_PAY_PER_BLOCK           = (GRAPHENE_BLOCKCHAIN_PRECISION * 10);
    public static final int GRAPHENE_DEFAULT_WITNESS_PAY_VESTING_SECONDS      = (60*60*24);
    public static final long GRAPHENE_DEFAULT_WORKER_BUDGET_PER_DAY           = (GRAPHENE_BLOCKCHAIN_PRECISION * 500 * 1000 );

    public static final int GRAPHENE_DEFAULT_MINIMUM_FEEDS                    = 7;

    public static final int GRAPHENE_MAX_INTEREST_APR                         = 10000; //uint16_t( 10000 )

    public static final int GRAPHENE_RECENTLY_MISSED_COUNT_INCREMENT          = 4;
    public static final int GRAPHENE_RECENTLY_MISSED_COUNT_DECREMENT          = 3;

    public static final String GRAPHENE_CURRENT_DB_VERSION                    = "GPH2.6";

    public static final int GRAPHENE_IRREVERSIBLE_THRESHOLD                   = (70 * GRAPHENE_1_PERCENT);

    /**
     *  Reserved Account IDs with special meaning
     */
    /*
///@{
/// Represents the current committee members, two-week review period
#define GRAPHENE_COMMITTEE_ACCOUNT (graphene::chain::account_id_type(0))
/// Represents the current witnesses
            #define GRAPHENE_WITNESS_ACCOUNT (graphene::chain::account_id_type(1))
/// Represents the current committee members
            #define GRAPHENE_RELAXED_COMMITTEE_ACCOUNT (graphene::chain::account_id_type(2))
/// Represents the canonical account with NO authority (nobody can access funds in null account)
            #define GRAPHENE_NULL_ACCOUNT (graphene::chain::account_id_type(3))
/// Represents the canonical account with WILDCARD authority (anybody can access funds in temp account)
            #define GRAPHENE_TEMP_ACCOUNT (graphene::chain::account_id_type(4))
/// Represents the canonical account for specifying you will vote directly (as opposed to a proxy)
            #define GRAPHENE_PROXY_TO_SELF_ACCOUNT (graphene::chain::account_id_type(5))
/// Sentinel value used in the scheduler.
            #define GRAPHENE_NULL_WITNESS (graphene::chain::witness_id_type(0))
///@}

            #define GRAPHENE_FBA_STEALTH_DESIGNATED_ASSET (asset_id_type(1))*/

    public static final object_id<asset_object> asset_object_base = new object_id<asset_object>(0, asset_object.class);
}
