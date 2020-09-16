package com.guarda.ethereum.models.items

data class SaplingTreeModel(
    val treeStates: List<SaplingBlockTree>
)

data class SaplingBlockTree(
    val network: String,
    val height: Long,
    val hash: String,
    val time: Long,
    val tree: String
)