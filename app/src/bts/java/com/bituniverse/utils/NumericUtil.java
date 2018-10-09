package com.bituniverse.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * <pre>
 * 数值相关工具类
 * </pre>
 *
 * @author: piorpua
 * @since: 2017/9/21
 */
public final class NumericUtil {

    private NumericUtil() {}

    public static long parseLong(@Nullable String value) {
        return parseLong(value, 0L);
    }

    public static long parseLong(@Nullable String value, long defaultVal) {
        if (TextUtils.isEmpty(value)) {
            return defaultVal;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defaultVal;
    }

    public static double parseDouble(@Nullable String value) {
        return parseDouble(value, 0.0D);
    }

    public static double parseDouble(@Nullable String value, double defaultVal) {
        if (TextUtils.isEmpty(value)) {
            return defaultVal;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defaultVal;
    }
}
