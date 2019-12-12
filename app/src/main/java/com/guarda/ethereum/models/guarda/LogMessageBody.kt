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
)
