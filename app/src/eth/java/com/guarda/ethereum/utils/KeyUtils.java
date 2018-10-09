package com.guarda.ethereum.utils;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.util.IllegalFormatCodePointException;

/**
 * Created by samosudovd on 13/03/2018.
 */

public class KeyUtils {

    public static boolean isValidPrivateKey(String privateKey) {
        if ((Numeric.cleanHexPrefix(privateKey).matches("[ABCDEFXabcdefx1234567890]+")) && WalletUtils.isValidPrivateKey(privateKey.toLowerCase())) {
            try {
                Credentials.create(privateKey);
                return true;
            } catch (NumberFormatException e) {
                return false;
            } catch (IllegalFormatCodePointException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
