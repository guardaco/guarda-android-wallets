package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by SV on 18.08.2017.
 */

public class TokensTxListResponse {

    @SerializedName("transfers")
    @Expose
    private List<TokenTxResponse> result;

    public List<TokenTxResponse> getResult() {
        return result;
    }
}
