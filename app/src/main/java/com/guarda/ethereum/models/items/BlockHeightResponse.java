package com.guarda.ethereum.models.items;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BlockHeightResponse {
    @SerializedName("blocks")
    @Expose
    private ArrayList<Block> blocksList;

    public Long getLastBlockHeight(){
        if (blocksList.size() != 0) {
            return blocksList.get(0).blockHeight;
        } else {
            return null;
        }
    }

    public class Block {
        @SerializedName("height")
        @Expose
        private Long blockHeight;

        @SerializedName("hash")
        @Expose
        private String blockHash;

        public Long getBlockHeight() {
            return blockHeight;
        }

        public String getBlockHash() {
            return blockHash;
        }
    }
}
