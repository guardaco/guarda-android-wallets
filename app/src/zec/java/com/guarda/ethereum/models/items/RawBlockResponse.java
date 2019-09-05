package com.guarda.ethereum.models.items;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RawBlockResponse {

    @SerializedName("rawblock")
    @Expose
    private String rawblock;

    public String getRawblock() {
        return rawblock;
    }
}