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

import android.os.AsyncTask;
import android.util.Log;

import com.guarda.ethereum.BuildConfig;

import org.greenrobot.eventbus.EventBus;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.api.responses.error.NetworkErrorType;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

public class ExchangeRateUpdateTaskHandler extends AsyncTask<ExchangeRateStorage, String, String> {

    private final Currency baseCurrency;
    private final Currency preferredCurrency;
    private final boolean updateSelective;
    private Boolean networkError = false;

    public ExchangeRateUpdateTaskHandler(Currency baseCurrency, Currency preferredCurrency, boolean updateSelective) {
        this.baseCurrency = baseCurrency;
        this.preferredCurrency = preferredCurrency;
        this.updateSelective = updateSelective;
    }

    @Override
    protected String doInBackground(ExchangeRateStorage... params) {
        try {
            ExchangeRateUpdater exchangeRateUpdater = new ExchangeRateUpdater(baseCurrency, params[0]);

        if (updateSelective) {
            exchangeRateUpdater.updateResourceEfficient(new CurrencyPair(Currency.BTC, preferredCurrency));

            if (BuildConfig.DEBUG)
                Log.d(getClass().getName(), "Updating price selective");

        }else

        if (BuildConfig.DEBUG)
            Log.d(getClass().getName(), "Updating price");

            exchangeRateUpdater.update();

        } catch (Exception e) {
            networkError = true;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        EventBus bus = EventBus.getDefault();

        if (networkError) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.EXCHANGE_RATE_ERROR);
            bus.post(error);
        } else
            bus.post(new ExchangeRateUpdateCompleted());
    }

    public class ExchangeRateUpdateCompleted {
    }
}
