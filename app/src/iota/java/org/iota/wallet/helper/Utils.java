/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.iota.wallet.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.widget.AbsListView;
import android.widget.ListView;

import org.knowm.xchange.currency.Currency;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class provides some utility method used across the app
 */
public class Utils {

    public static void fixListView(final ListView lv, final SwipeRefreshLayout swipeLayout) {
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lv == null || lv.getChildCount() == 0) ? 0 : lv.getChildAt(0).getTop();
                swipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    /**
     * @return the currency of the wallet
     */
    public static Currency getBaseCurrency() {
        return new Currency("IOT");
    }

    public static Currency getConfiguredAlternateCurrency(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new Currency(prefs.getString(Constants.PREFERENCE_WALLET_VALUE_CURRENCY, "BTC"));
    }

    public static String timeStampToDate(long timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp * 1000);
        return df.format(date);
    }

    public static File getExternalIotaDirectory(Context context) {
        try {
            File cacheDir = new File(context.getExternalCacheDir(), "iota");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            return cacheDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int createNewID(){
        Date now = new Date();
        return Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
    }
}

