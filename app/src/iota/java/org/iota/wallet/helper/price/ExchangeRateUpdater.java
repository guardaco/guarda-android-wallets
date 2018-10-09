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


import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.okcoin.OkCoinExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class ExchangeRateUpdater {

    private final CurrencyPair baseCurrencyBtcPair;

    private final ExchangeRateStorage storage;
    private final Map<CurrencyPair, Exchange> exchanges;

    public ExchangeRateUpdater(Currency baseCurrency, ExchangeRateStorage priceStorage) {
        this.storage = priceStorage;
        this.baseCurrencyBtcPair = new CurrencyPair(baseCurrency, Currency.BTC);

        exchanges = new HashMap<>();
        exchanges.put(baseCurrencyBtcPair, ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName()));
        exchanges.put(CurrencyPair.BTC_USD, ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName()));
        exchanges.put(CurrencyPair.BTC_EUR, ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName()));
        exchanges.put(CurrencyPair.BTC_CNY, ExchangeFactory.INSTANCE.createExchange(OkCoinExchange.class.getName()));

        // add more currencies/exchange pairs (btc/fiat) here
    }

    public void update() {
        for (CurrencyPair currencyPair : exchanges.keySet()) {
            update(currencyPair);
        }
    }

    private void update(CurrencyPair currencyPair) {
        try {
            if (exchanges.containsKey(currencyPair)) {
                BigDecimal price = exchanges.get(currencyPair)
                        .getMarketDataService()
                        .getTicker(currencyPair)
                        .getLast();

                storage.setExchangeRate(currencyPair, price.floatValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateResourceEfficient(CurrencyPair preferredCurrencyPair) {

        // update base/btc
        update(baseCurrencyBtcPair);
        // update btc/preferred currency
        if (!baseCurrencyBtcPair.equals(preferredCurrencyPair))
            update(preferredCurrencyPair);
    }
}
