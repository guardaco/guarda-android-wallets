package com.guarda.ethereum.models.guarda


data class LogMessageBody (
        val fromAddress: String,
        val toAddress: String,
        val amount: String,
        val fee: String,
        val isInclude: Boolean,
        val isSyncing: Boolean,
        val isFromShielded: Boolean,
        val tBalance: String,
        val zBalance: String,
        val errorMessage: String
) {

    override fun toString(): String {
        return "LogMessageBody(" +
                "fromAddress='$fromAddress', " +
                "toAddress='$toAddress', " +
                "amount='$amount', " +
                "fee='$fee', " +
                "isInclude=$isInclude, " +
                "isSyncing=$isSyncing, " +
                "isFromShielded=$isFromShielded, " +
                "tBalance='$tBalance', " +
                "zBalance='$zBalance', " +
                "errorMessage='$errorMessage'" +
                ")"
    }
}
