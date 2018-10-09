package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.account_object;

import java.util.List;

import static com.bitshares.bitshareswallet.wallet.graphene.chain.config.*;
import static com.bitshares.bitshareswallet.wallet.graphene.chain.types.UIA_ASSET_ISSUER_PERMISSION_MASK;


public class asset_options {
    /// The maximum supply of this asset which may exist at any given time. This can be as large as
    /// GRAPHENE_MAX_SHARE_SUPPLY
    long max_supply = GRAPHENE_MAX_SHARE_SUPPLY;
    /// When this asset is traded on the markets, this percentage of the total traded will be exacted and paid
    /// to the issuer. This is a fixed point value, representing hundredths of a percent, i.e. a value of 100
    /// in this field means a 1% fee is charged on market trades of this asset.
    int market_fee_percent = 0;
    /// Market fees calculated as @ref market_fee_percent of the traded volume are capped to this value
    long max_market_fee = GRAPHENE_MAX_SHARE_SUPPLY;

    /// The flags which the issuer has permission to update. See @ref asset_issuer_permission_flags
    int issuer_permissions = UIA_ASSET_ISSUER_PERMISSION_MASK;
    /// The currently active flags on this permission. See @ref asset_issuer_permission_flags
    int flags = 0;

    /// When a non-core asset is used to pay a fee, the blockchain must convert that asset to core asset in
    /// order to accept the fee. If this asset's fee pool is funded, the chain will automatically deposite fees
    /// in this asset to its accumulated fees, and withdraw from the fee pool the same amount as converted at
    /// the core exchange rate.
    public price core_exchange_rate;

    /// A set of accounts which maintain whitelists to consult for this asset. If whitelist_authorities
    /// is non-empty, then only accounts in whitelist_authorities are allowed to hold, use, or transfer the asset.
    List<object_id<account_object>> whitelist_authorities;
    /// A set of accounts which maintain blacklists to consult for this asset. If flags & white_list is set,
    /// an account may only send, receive, trade, etc. in this asset if none of these accounts appears in
    /// its account_object::blacklisting_accounts field. If the account is blacklisted, it may not transact in
    /// this asset even if it is also whitelisted.
    List<object_id<account_object>> blacklist_authorities;

    /** defines the assets that this asset may be traded against in the market */
    List<object_id<asset_object>>   whitelist_markets;
    /** defines the assets that this asset may not be traded against in the market, must not overlap whitelist */
    List<object_id<asset_object>>   blacklist_markets;

    /**
     * data that describes the meaning/purpose of this asset, fee will be charged proportional to
     * size of description.
     */
    String description;
    //extensions_type extensions;

    /// Perform internal consistency checks.
    /// @throws fc::exception if any check fails
}
