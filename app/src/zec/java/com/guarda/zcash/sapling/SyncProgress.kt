package com.guarda.zcash.sapling

class SyncProgress(
        var fromBlock: Long = 0,
        var toBlock: Long = 0,
        var currentBlock: Long = 0,
        var processPhase: String = DOWNLOAD_PHASE
) {

    companion object {
        const val DOWNLOAD_PHASE = "Downloading"
        const val SEARCH_PHASE = "Searching"
        const val SYNCED_PHASE = "Synced"
    }

    override fun toString(): String {
        return "SyncProgress(fromBlock=$fromBlock, toBlock=$toBlock, currentBlock=$currentBlock, processPhase='$processPhase')"
    }


}