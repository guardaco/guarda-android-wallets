package com.guarda.ethereum.models.guarda

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class LogMessageRequest (
        @Expose
        @SerializedName("platform")
        val platform: String? = LOGGER_PLATFORM,
        @Expose
        @SerializedName("device")
        val device: String? = "",
        @Expose
        @SerializedName("version")
        val version: String? = "",
        @Expose
        @SerializedName("env")
        val env: String? = LOGGER_ENV,
        @Expose
        @SerializedName("createdAt")
        val createdAt: String? = "",
        @Expose
        @SerializedName("type")
        val type: String? = "",
        @Expose
        @SerializedName("body")
        val body: String? = ""
) {
        companion object {
                const val LOGGER_PLATFORM = "mono/android"
                const val LOGGER_ENV = "mono/production"
        }
}
