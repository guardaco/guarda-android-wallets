package com.guarda.ethereum.models.changenow

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by samosudovd on 07/06/2018.
 */

data class MinAmountResp (
        @Expose
        @SerializedName("minAmount")
        var minAmount: Float = 0.toFloat())
