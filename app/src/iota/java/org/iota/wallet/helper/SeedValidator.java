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

import org.apache.commons.lang3.StringUtils;

public class SeedValidator {

    private static final int SEED_LENGTH_MIN = 41;
    private static final int SEED_LENGTH_MAX = 81;

    public static String isSeedValid(Context context, String seed) {
        if (!seed.matches("^[A-Z9a-z]+$")) {
            if (seed.length() > SEED_LENGTH_MAX)
                return "messages_invalid_characters_seed" + " " + "messages_seed_to_long";
            else if (seed.length() < SEED_LENGTH_MIN)
                return "messages_invalid_characters_seed" + " " + "messages_seed_to_short";
            else
            return "messages_invalid_characters_seed";

        } else if (seed.matches(".*[A-Z].*") && seed.matches(".*[a-z].*")) {
            if (seed.length() > SEED_LENGTH_MAX)
                return "messages_mixed_seed" + " " + "messages_seed_to_long";
            else if (seed.length() < SEED_LENGTH_MIN)
                return "messages_mixed_seed" + " " + "messages_seed_to_short";
            else
                return "messages_mixed_seed";

        } else if (seed.length() > SEED_LENGTH_MAX) {
            return "messages_to_long_seed";

        } else if (seed.length() < SEED_LENGTH_MIN) {
            return "messages_to_short_seed";
        }

        return null;
    }

    public static String getSeed(String seed) {

        seed = seed.toUpperCase();

        if (seed.length() > SEED_LENGTH_MAX)
            seed = seed.substring(0, SEED_LENGTH_MAX);

        seed = seed.replaceAll("[^A-Z9]", "9");

        seed = StringUtils.rightPad(seed, SEED_LENGTH_MAX, '9');

        return seed;
    }
}
