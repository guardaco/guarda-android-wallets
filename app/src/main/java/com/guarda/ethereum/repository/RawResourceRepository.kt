package com.guarda.ethereum.repository

import com.guarda.ethereum.R
import com.guarda.ethereum.managers.FileProvider
import com.guarda.ethereum.models.items.SaplingBlockTree
import com.guarda.ethereum.utils.GsonUtils

class RawResourceRepository(
    private val fileProvider: FileProvider,
    private val gsonUtils: GsonUtils
) {

    lateinit var treeStates: List<SaplingBlockTree>

    init {
        initTreeStates()
    }

    private fun initTreeStates() {
        val saplingTreeStates = saplingTreeStatesString()
        val saplingTreeModel = gsonUtils.saplingTreeModel(saplingTreeStates)
        treeStates = saplingTreeModel.treeStates
    }

    private fun saplingTreeStatesString() = fileProvider.getFileStringFromRawRes(R.raw.saplingtreestates)

}