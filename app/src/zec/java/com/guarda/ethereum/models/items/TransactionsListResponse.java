package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by SV on 18.08.2017.
 */

public class TransactionsListResponse {

    @SerializedName("result")
    @Expose
    private List<TransactionResponse> result;

    public List<TransactionResponse> getResult() {
        return result;
    }
}
