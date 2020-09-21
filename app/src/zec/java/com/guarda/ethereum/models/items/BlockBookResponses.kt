package com.guarda.ethereum.models.items

import com.google.gson.annotations.SerializedName

data class BlockBookResponse(
    @SerializedName("addrStr")
    val addrStr: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("txs")
    val txs: List<BlockBookTx>
)

data class BlockBookTx(
    @SerializedName("txid")
    val txid: String,
    @SerializedName("vin")
    val vin: List<BlockBookVin>,
    @SerializedName("vout")
    val vout: List<BlockBookVout>,
    @SerializedName("confirmations")
    val confirmations: Int,
    @SerializedName("time")
    val time: Long
)

data class BlockBookVin(
    @SerializedName("addresses")
    val addresses: List<String>?,
    @SerializedName("value")
    val value: String
)

data class BlockBookVout(
    @SerializedName("value")
    val value: String,
    @SerializedName("scriptPubKey")
    val scriptPubKey: BlockBookScriptPk
)

data class BlockBookScriptPk(
    @SerializedName("addresses")
    val addresses: List<String>
)

data class BlockBookBlock(
        val height: Long,
        val time: Long
)