package com.guarda.ethereum.models.changenow

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by samosudovd on 07/06/2018.
 */

data class EstimatedResp (
        @Expose
        @SerializedName("estimatedAmount")
        val estimatedAmount: Float = 0.toFloat(),
        @Expose
        @SerializedName("transactionSpeedForecast")
        val transactionSpeedForecast: String? = null,
        @Expose
        @SerializedName("warningMessage")
        val warningMessage: String? = null)
