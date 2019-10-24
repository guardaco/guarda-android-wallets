package com.guarda.ethereum.models.changenow

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by samosudovd on 07/06/2018.
 */

data class TxResp (
        @Expose
        @SerializedName("id")
        val id: String? = null,
        @Expose
        @SerializedName("status")
        val status: String? = null,
        @Expose
        @SerializedName("hash")
        val hash: String? = null,
        @Expose
        @SerializedName("payinHash")
        val payinHash: String? = null,
        @Expose
        @SerializedName("payoutHash")
        val payoutHash: String? = null,
        @Expose
        @SerializedName("payinAddress")
        val payinAddress: String? = null,
        @Expose
        @SerializedName("payoutAddress")
        val payoutAddress: String? = null,
        @Expose
        @SerializedName("payinExtraId")
        val payinExtraId: String? = null,
        @Expose
        @SerializedName("payoutExtraId")
        val payoutExtraId: String? = null,
        @Expose
        @SerializedName("fromCurrency")
        val fromCurrency: String? = null,
        @Expose
        @SerializedName("toCurrency")
        val toCurrency: String? = null,
        @Expose
        @SerializedName("amountSend")
        val amountSend: String? = null,
        @Expose
        @SerializedName("amountReceive")
        val amountReceive: String? = null,
        @Expose
        @SerializedName("networkFee")
        val networkFee: String? = null,
        @Expose
        @SerializedName("updatedAt")
        val updatedAt: String? = null,
        @Expose
        @SerializedName("expectedSendAmount")
        val expectedSendAmount: String? = null,
        @Expose
        @SerializedName("expectedReceiveAmount")
        val expectedReceiveAmount: String? = null)
