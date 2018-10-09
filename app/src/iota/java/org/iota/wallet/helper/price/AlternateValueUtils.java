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

import java.text.DecimalFormat;

public class AlternateValueUtils {

    public static String formatAlternateBalanceText(float value, Currency currency) {
        return new DecimalFormat("##0.00").format(value) + " " + getSymbol(currency);
    }

    private static String getSymbol(Currency currency) {
        if (currency.equals(Currency.BTC))
            return "Ƀ";
        else if (currency.equals(Currency.USD))
            return "$";
        else if (currency.equals(Currency.EUR))
            return "€";
        else if (currency.equals(Currency.CNY))
            return "¥";

        // should never happen:
        return "";
    }

}
