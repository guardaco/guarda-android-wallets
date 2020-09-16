package com.guarda.ethereum.managers

import android.content.Context
import okio.BufferedSource
import okio.Source
import okio.buffer
import okio.source


class FileProvider(val context: Context) {

    fun getFileStringFromRawRes(rawResId: Int) : String {
        val source: Source = context.resources.openRawResource(rawResId).source()
        val bufferedSource: BufferedSource = source.buffer()

        var fileString = ""
        while (true) {
            val line: String? = bufferedSource.readUtf8Line()
            line ?: break
            fileString += line
        }
        return fileString
    }

}