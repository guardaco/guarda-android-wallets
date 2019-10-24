package com.guarda.ethereum.models.changenow

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by samosudovd on 07/06/2018.
 *
 * for sorting like in https://api.coinmarketcap.com/v2/listings/
 */


data class CurrencyResp (
        @Expose
        @SerializedName("id")
        val id: String? = null,
        @Expose
        @SerializedName("ticker")
        val ticker: String? = null,
        @Expose
        @SerializedName("name")
        val name: String? = null,
        @Expose
        @SerializedName("image")
        val image: String? = null,
        @Expose
        @SerializedName("hasExternalId")
        val isHasExternalId: Boolean = false,
        @Expose
        @SerializedName("isFiat")
        val isFiat: Boolean = false,
        @Expose
        @SerializedName("isAvailable")
        val isAvailable: Boolean = false)
