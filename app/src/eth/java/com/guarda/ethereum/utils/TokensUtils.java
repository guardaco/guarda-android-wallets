package com.guarda.ethereum.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guarda.ethereum.managers.Converter;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.items.AddressInfoResp;
import com.guarda.ethereum.models.items.TokenBodyItem;
import com.guarda.ethereum.models.items.TokenListItem;
import com.guarda.ethereum.models.items.TransactionResponse;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Map;

/**
 * Created by samosudovd on 13/03/2018.
 */

public class TokensUtils {

    public static HashMap<String, TokenListItem> fillMapContractToken(List<TokenListItem> tokenList) {
        HashMap<String, TokenListItem> mapContractToken = new HashMap<>();
        for (TokenListItem tokenListItem : tokenList) {
            mapContractToken.put(tokenListItem.getSmartContract().toLowerCase(), tokenListItem);
        }
        return mapContractToken;
    }

    public static void setMapContractToken(SharedManager sharedManager, HashMap<String, TokenListItem> mapContractToken) {
        Gson gson = new Gson();
        String jsonToPref = gson.toJson(mapContractToken);
        sharedManager.setMapTokens(jsonToPref);
    }

    public static HashMap<String, TokenListItem> getMapContractToken(String mapFromPref) {
        Gson gson = new Gson();
        Type listType = new TypeToken<HashMap<String, TokenListItem>>(){}.getType();
        return gson.fromJson(mapFromPref, listType);
    }

    public static List<TokenBodyItem> setTokenList(AddressInfoResp addressInfoResp) {
        List<TokenBodyItem> tokensList = new ArrayList<>();
        for (AddressInfoResp.Token token : addressInfoResp.getTokens()) {
            BigDecimal formatted = Converter.fromDecimals(token.getBalance(), token.getTokenInfo().getDecimals());
            tokensList.add(new TokenBodyItem(token.getTokenInfo().getSymbol(), formatted, "", token.getTokenInfo().getDecimals()));
        }
        return tokensList;
    }

}
