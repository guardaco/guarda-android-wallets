package work.samosudov.rustlib.crypto;

/**
 * Copyright (c) 2018 Tobias Brandt
 *
 * Copyright (c) 2017 Pieter Wuille
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
public class BitcoinCashBitArrayConverter {

    public static byte[] convertBits(byte[] bytes8Bits, int from, int to, boolean strictMode) {
        int length = (int) (strictMode ? Math.floor((double) bytes8Bits.length * from / to)
                : Math.ceil((double) bytes8Bits.length * from / to));
        int mask = ((1 << to) - 1) & 0xff;
        byte[] result = new byte[length];
        int index = 0;
        int accumulator = 0;
        int bits = 0;
        for (int i = 0; i < bytes8Bits.length; i++) {
            byte value = bytes8Bits[i];
            accumulator = (((accumulator & 0xff) << from) | (value & 0xff));
            bits += from;
            while (bits >= to) {
                bits -= to;
                result[index] = (byte) ((accumulator >> bits) & mask);
                ++index;
            }
        }
        if (!strictMode) {
            if (bits > 0) {
                result[index] = (byte) ((accumulator << (to - bits)) & mask);
                ++index;
            }
        } else {
            if (!(bits < from && ((accumulator << (to - bits)) & mask) == 0)) {
                throw new RuntimeException("Strict mode was used but input couldn't be converted without padding");
            }
        }

        return result;
    }

}