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

package org.iota.wallet.helper.price;

import android.content.SharedPreferences;

import org.iota.wallet.helper.Constants;
import org.knowm.xchange.currency.CurrencyPair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExchangeRateStorage implements IExchangeRateProvider {

    private static final String TIMESTAMP = "timestamp";
    private final SharedPreferences sharedPreferences;

    public ExchangeRateStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public synchronized void setExchangeRate(CurrencyPair currencyPair, float rate) {
        String sharedPrefKey = getKey(currencyPair);

        sharedPreferences.edit()
                .putFloat(sharedPrefKey, rate)
                .putString(getTimeStampKey(currencyPair), createTimeStamp())
                .apply();
    }

    @Override
    public synchronized float getExchangeRate(CurrencyPair currencyPair) throws ExchangeRateNotAvailableException {
        float NOT_AVAILABLE = -1;
        float exchangeRate = sharedPreferences.getFloat(getKey(currencyPair), NOT_AVAILABLE);

        if (exchangeRate == NOT_AVAILABLE)
            throw new ExchangeRateNotAvailableException(currencyPair);

        return exchangeRate;
    }

    public synchronized String getTimeStamp(CurrencyPair currencyPair) {
        return sharedPreferences.getString(getTimeStampKey(currencyPair), "-");
    }

    private String getKey(CurrencyPair currencyPair) {
        return Constants.PRICE_STORAGE_PREFIX + "_" + currencyPair.toString();
    }

    private String getTimeStampKey(CurrencyPair currencyPair) {
        return getKey(currencyPair) + "_" + TIMESTAMP;
    }

    private String createTimeStamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        return df.format(new Date());
    }
}
