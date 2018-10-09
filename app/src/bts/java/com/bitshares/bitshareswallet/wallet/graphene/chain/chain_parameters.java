package com.bitshares.bitshareswallet.wallet.graphene.chain;

import static com.bitshares.bitshareswallet.wallet.graphene.chain.config.*;


public class chain_parameters {
    public fee_schedule current_fees;                       ///< current schedule of fees
    public int block_interval = GRAPHENE_DEFAULT_BLOCK_INTERVAL; ///< interval in seconds between blocks
    public int maintenance_interval = GRAPHENE_DEFAULT_MAINTENANCE_INTERVAL; ///< interval in sections between blockchain maintenance events
    public int maintenance_skip_slots = GRAPHENE_DEFAULT_MAINTENANCE_SKIP_SLOTS; ///< number of block_intervals to skip at maintenance time
    public int committee_proposal_review_period = GRAPHENE_DEFAULT_COMMITTEE_PROPOSAL_REVIEW_PERIOD_SEC; ///< minimum time in seconds that a proposed transaction requiring committee authority may not be signed, prior to expiration
    public int maximum_transaction_size = GRAPHENE_DEFAULT_MAX_TRANSACTION_SIZE; ///< maximum allowable size in bytes for a transaction
    public int maximum_block_size = GRAPHENE_DEFAULT_MAX_BLOCK_SIZE; ///< maximum allowable size in bytes for a block
    public int maximum_time_until_expiration = GRAPHENE_DEFAULT_MAX_TIME_UNTIL_EXPIRATION; ///< maximum lifetime in seconds for transactions to be valid, before expiring
    public int maximum_proposal_lifetime = GRAPHENE_DEFAULT_MAX_PROPOSAL_LIFETIME_SEC; ///< maximum lifetime in seconds for proposed transactions to be kept, before expiring
    public int maximum_asset_whitelist_authorities = GRAPHENE_DEFAULT_MAX_ASSET_WHITELIST_AUTHORITIES; ///< maximum number of accounts which an asset may list as authorities for its whitelist OR blacklist
    public int maximum_asset_feed_publishers = GRAPHENE_DEFAULT_MAX_ASSET_FEED_PUBLISHERS; ///< the maximum number of feed publishers for a given asset
    public int maximum_witness_count = GRAPHENE_DEFAULT_MAX_WITNESSES; ///< maximum number of active witnesses
    public int maximum_committee_count = GRAPHENE_DEFAULT_MAX_COMMITTEE; ///< maximum number of active committee_members
    public int maximum_authority_membership = GRAPHENE_DEFAULT_MAX_AUTHORITY_MEMBERSHIP; ///< largest number of keys/accounts an authority can have
    public int reserve_percent_of_fee = GRAPHENE_DEFAULT_BURN_PERCENT_OF_FEE; ///< the percentage of the network's allocation of a fee that is taken out of circulation
    public int network_percent_of_fee = GRAPHENE_DEFAULT_NETWORK_PERCENT_OF_FEE; ///< percent of transaction fees paid to network
    public int lifetime_referrer_percent_of_fee = GRAPHENE_DEFAULT_LIFETIME_REFERRER_PERCENT_OF_FEE; ///< percent of transaction fees paid to network
    public int cashback_vesting_period_seconds = GRAPHENE_DEFAULT_CASHBACK_VESTING_PERIOD_SEC; ///< time after cashback rewards are accrued before they become liquid
    public long cashback_vesting_threshold = GRAPHENE_DEFAULT_CASHBACK_VESTING_THRESHOLD; ///< the maximum cashback that can be received without vesting
    public boolean count_non_member_votes = true; ///< set to false to restrict voting privlegages to member accounts
    public boolean allow_non_member_whitelists = false; ///< true if non-member accounts may set whitelists and blacklists; false otherwise
    public long witness_pay_per_block = GRAPHENE_DEFAULT_WITNESS_PAY_PER_BLOCK; ///< CORE to be allocated to witnesses (per block)
    public int witness_pay_vesting_seconds = GRAPHENE_DEFAULT_WITNESS_PAY_VESTING_SECONDS; ///< vesting_seconds parameter for witness VBO's
    public long worker_budget_per_day = GRAPHENE_DEFAULT_WORKER_BUDGET_PER_DAY; ///< CORE to be allocated to workers (per day)
    public int max_predicate_opcode = GRAPHENE_DEFAULT_MAX_ASSERT_OPCODE; ///< predicate_opcode must be less than this number
    public long fee_liquidation_threshold = GRAPHENE_DEFAULT_FEE_LIQUIDATION_THRESHOLD; ///< value in CORE at which accumulated fees in blockchain-issued market assets should be liquidated
    public int accounts_per_fee_scale = GRAPHENE_DEFAULT_ACCOUNTS_PER_FEE_SCALE; ///< number of accounts between fee scalings
    public int account_fee_scale_bitshifts = GRAPHENE_DEFAULT_ACCOUNT_FEE_SCALE_BITSHIFTS; ///< number of times to left bitshift account registration fee at each scaling
    public int max_authority_depth = GRAPHENE_MAX_SIG_CHECK_DEPTH;
    //extensions_type         extensions;

    /*smart_ref<fee_schedule> current_fees;                       ///< current schedule of fees
    uint8_t                 block_interval                      = GRAPHENE_DEFAULT_BLOCK_INTERVAL; ///< interval in seconds between blocks
    uint32_t                maintenance_interval                = GRAPHENE_DEFAULT_MAINTENANCE_INTERVAL; ///< interval in sections between blockchain maintenance events
    uint8_t                 maintenance_skip_slots              = GRAPHENE_DEFAULT_MAINTENANCE_SKIP_SLOTS; ///< number of block_intervals to skip at maintenance time
    uint32_t                committee_proposal_review_period    = GRAPHENE_DEFAULT_COMMITTEE_PROPOSAL_REVIEW_PERIOD_SEC; ///< minimum time in seconds that a proposed transaction requiring committee authority may not be signed, prior to expiration
    uint32_t                maximum_transaction_size            = GRAPHENE_DEFAULT_MAX_TRANSACTION_SIZE; ///< maximum allowable size in bytes for a transaction
    uint32_t                maximum_block_size                  = GRAPHENE_DEFAULT_MAX_BLOCK_SIZE; ///< maximum allowable size in bytes for a block
    uint32_t                maximum_time_until_expiration       = GRAPHENE_DEFAULT_MAX_TIME_UNTIL_EXPIRATION; ///< maximum lifetime in seconds for transactions to be valid, before expiring
    uint32_t                maximum_proposal_lifetime           = GRAPHENE_DEFAULT_MAX_PROPOSAL_LIFETIME_SEC; ///< maximum lifetime in seconds for proposed transactions to be kept, before expiring
    uint8_t                 maximum_asset_whitelist_authorities = GRAPHENE_DEFAULT_MAX_ASSET_WHITELIST_AUTHORITIES; ///< maximum number of accounts which an asset may list as authorities for its whitelist OR blacklist
    uint8_t                 maximum_asset_feed_publishers       = GRAPHENE_DEFAULT_MAX_ASSET_FEED_PUBLISHERS; ///< the maximum number of feed publishers for a given asset
    uint16_t                maximum_witness_count               = GRAPHENE_DEFAULT_MAX_WITNESSES; ///< maximum number of active witnesses
    uint16_t                maximum_committee_count             = GRAPHENE_DEFAULT_MAX_COMMITTEE; ///< maximum number of active committee_members
    uint16_t                maximum_authority_membership        = GRAPHENE_DEFAULT_MAX_AUTHORITY_MEMBERSHIP; ///< largest number of keys/accounts an authority can have
    uint16_t                reserve_percent_of_fee              = GRAPHENE_DEFAULT_BURN_PERCENT_OF_FEE; ///< the percentage of the network's allocation of a fee that is taken out of circulation
    uint16_t                network_percent_of_fee              = GRAPHENE_DEFAULT_NETWORK_PERCENT_OF_FEE; ///< percent of transaction fees paid to network
    uint16_t                lifetime_referrer_percent_of_fee    = GRAPHENE_DEFAULT_LIFETIME_REFERRER_PERCENT_OF_FEE; ///< percent of transaction fees paid to network
    uint32_t                cashback_vesting_period_seconds     = GRAPHENE_DEFAULT_CASHBACK_VESTING_PERIOD_SEC; ///< time after cashback rewards are accrued before they become liquid
    share_type              cashback_vesting_threshold          = GRAPHENE_DEFAULT_CASHBACK_VESTING_THRESHOLD; ///< the maximum cashback that can be received without vesting
    bool                    count_non_member_votes              = true; ///< set to false to restrict voting privlegages to member accounts
    bool                    allow_non_member_whitelists         = false; ///< true if non-member accounts may set whitelists and blacklists; false otherwise
    share_type              witness_pay_per_block               = GRAPHENE_DEFAULT_WITNESS_PAY_PER_BLOCK; ///< CORE to be allocated to witnesses (per block)
    uint32_t                witness_pay_vesting_seconds         = GRAPHENE_DEFAULT_WITNESS_PAY_VESTING_SECONDS; ///< vesting_seconds parameter for witness VBO's
    share_type              worker_budget_per_day               = GRAPHENE_DEFAULT_WORKER_BUDGET_PER_DAY; ///< CORE to be allocated to workers (per day)
    uint16_t                max_predicate_opcode                = GRAPHENE_DEFAULT_MAX_ASSERT_OPCODE; ///< predicate_opcode must be less than this number
    share_type              fee_liquidation_threshold           = GRAPHENE_DEFAULT_FEE_LIQUIDATION_THRESHOLD; ///< value in CORE at which accumulated fees in blockchain-issued market assets should be liquidated
    uint16_t                accounts_per_fee_scale              = GRAPHENE_DEFAULT_ACCOUNTS_PER_FEE_SCALE; ///< number of accounts between fee scalings
    uint8_t                 account_fee_scale_bitshifts         = GRAPHENE_DEFAULT_ACCOUNT_FEE_SCALE_BITSHIFTS; ///< number of times to left bitshift account registration fee at each scaling
    uint8_t                 max_authority_depth                 = GRAPHENE_MAX_SIG_CHECK_DEPTH;
    extensions_type         extensions;*/
}
