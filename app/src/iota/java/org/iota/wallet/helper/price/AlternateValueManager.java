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

import android.content.Context;
import android.preference.PreferenceManager;

import org.iota.wallet.helper.Utils;
import org.knowm.xchange.currency.Currency;

import jota.utils.IotaUnits;

public class AlternateValueManager {

    private final Context context;

    public AlternateValueManager(Context context) {
        this.context = context;
    }

    public float convert(long iotaAmount, Currency currency) throws ExchangeRateNotAvailableException {
        Currency baseCurrency = Utils.getBaseCurrency();
        AlternateValueCalculator calculator = new AlternateValueCalculator(baseCurrency,
                new ExchangeRateStorage(PreferenceManager.getDefaultSharedPreferences(context)));

        // convert the iota to mega iota assuming that iota will be traded in mega iotas

        double walletBalanceGigaIota = jota.utils.IotaUnitConverter.convertUnits(iotaAmount, IotaUnits.IOTA, IotaUnits.MEGA_IOTA);
        return calculator.calculateValue((float) walletBalanceGigaIota, currency);
    }

    public void updateExchangeRatesAsync(boolean updateSelective) {
        ExchangeRateStorage storage = new ExchangeRateStorage(PreferenceManager.getDefaultSharedPreferences(context));

        ExchangeRateUpdateTask exchangeRateUpdateTask = new ExchangeRateUpdateTask(context, Utils.getBaseCurrency(),
                storage);
        exchangeRateUpdateTask.startNewRequestTask(updateSelective);
    }
}
