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

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

class AlternateValueCalculator {

    private final Currency baseCurrency; // currency of the altcoin (iota)
    private final IExchangeRateProvider exchangeRateProvider;

    public AlternateValueCalculator(Currency baseCurrency, IExchangeRateProvider exchangeRateProvider) {
        this.baseCurrency = baseCurrency;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    public float calculateValue(float baseAmount, Currency targetCurrency) throws ExchangeRateNotAvailableException {
        // convert the amount to btc first
        float btcAmount = calcBtcAmount(baseAmount);
        float targetValue;

        // if the target currency is btc we do nothing, otherwise we use the rate
        if (targetCurrency.equals(Currency.BTC)) {
            targetValue = btcAmount;
        } else {

            float fiatBtcPrice = exchangeRateProvider.getExchangeRate(new CurrencyPair(Currency.BTC, targetCurrency));
            targetValue = btcAmount * fiatBtcPrice;
        }

        return targetValue;
    }

    private float calcBtcAmount(float baseAmount) throws ExchangeRateNotAvailableException {
        float lastBaseBtcPrice = getBaseBtcPrice();
        return lastBaseBtcPrice * baseAmount;
    }

    // overwrite this method to get a fixed exchange rate
    private float getBaseBtcPrice() throws ExchangeRateNotAvailableException {
        return exchangeRateProvider.getExchangeRate(new CurrencyPair(baseCurrency, Currency.BTC));
    }
}
