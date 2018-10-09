package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.room.BitsharesAssetObject;

public class utils {

    public static boolean is_cheap_name(String strName) {
        boolean v = false;
        for (char c : strName.toCharArray()) {
            if (c >= '0' && c <= '9') return true;
            if (c == '.' || c == '-' || c == '/') return true;
            switch (c) {
                case 'a':
                case 'e':
                case 'i':
                case 'o':
                case 'u':
                case 'y':
                    v = true;
            }
        }
        if (!v)
            return true;

        return false;
    }

    public static double get_asset_amount(long amount, asset_object assetObject) {
        if (amount == 0) {
            return (double)amount;
        } else {
            return (double)amount / (double) assetObject.get_scaled_precision();
        }
    }

    public static double get_asset_price(long quoteAmount, asset_object quoteAsset,
                                         long baseAmount, asset_object baseAsset) {
        if (quoteAsset == null || baseAsset == null) {
            return 1;
        }
        try {
            return get_asset_amount(quoteAmount, quoteAsset) / get_asset_amount(baseAmount, baseAsset);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static double get_asset_amount(long amount, BitsharesAssetObject assetObject) {
        if (amount == 0) {
            return (double)amount;
        } else {
            return (double)amount / (double) assetObject.precision;
        }
    }

    public static double get_asset_price(long quoteAmount, BitsharesAssetObject quoteAsset,
                                         long baseAmount, BitsharesAssetObject baseAsset) {
        if (quoteAsset == null || baseAsset == null) {
            return 1;
        }
        try {
            return get_asset_amount(quoteAmount, quoteAsset) / get_asset_amount(baseAmount, baseAsset);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static String getAssetSymbolDisply(String strAssetSymbol) {
        return strAssetSymbol.replaceAll("OPEN.", "");
    }
}
