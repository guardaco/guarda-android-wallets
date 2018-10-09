package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SV on 18.08.2017.
 */

public class TransactionsListResponse {

    @SerializedName("data")
    @Expose
    private List<ArrayList<String>> data;

    public List<ArrayList<String>> getData() {
        return data;
    }
}
