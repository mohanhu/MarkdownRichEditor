package com.example.markdownapp.spanrichlib

import android.widget.EditText
import android.widget.TextView
import com.example.markdownapp.R
import com.example.markdownapp.spanrichlib.RichSpanDownStyle.makeStyleFormat
import com.example.markdownapp.spanrichlib.StyleActionBindClick.editAddLink
import com.example.markdownapp.spanrichlib.StyleActionBindClick.editAddMention
import com.google.gson.Gson

object BlockExport {

    fun EditText.blockJsonExportToEdit(blockJson: String){
        val blockKitData = Gson().fromJson(blockJson, BlockKitData::class.java)
        var blockKitManage = blockKitData.block.map { it.toBlockKitManage() }
        var makeString = ""
        blockKitData.block.forEachIndexed { index, head ->
            blockKitManage = blockKitManage.mapIndexed { i, b ->
                if (i == index) {
                    b.copy(startIndex = makeString.length, endIndex = makeString.length + b.word.length)
                } else {
                    b
                }
            }
            makeString += head.word
        }
        setText(makeString, TextView.BufferType.SPANNABLE)
        blockKitManage.sortedBy { it.startIndex }.forEach {
            if (it.styleFormat == Styles.MENTION){
                editAddMention(it.word,it.key.toLong(),it.startIndex,it.endIndex)
            }
            if ( it.styleFormat == Styles.LINK){
                editAddLink(it.word,it.key,it.startIndex,it.endIndex)
            }
            if (it.styleFormat == Styles.BOLD){
                makeStyleFormat(it.word,it.styleFormat,it.startIndex,it.endIndex)
            }
            if (it.styleFormat == Styles.ITALIC){
                makeStyleFormat(it.word,it.styleFormat,it.startIndex,it.endIndex)
            }
            println("mentionDataClass.sortedByDescending >>> ${it.styleFormat}")
        }
    }

    fun TextView.blockJsonToExportDataTextView(blockJson: String): List<MentionDataClass> {
        val updateFormat = Gson().fromJson(blockJson, BlockKitData::class.java)
        var blockList = updateFormat.block.map { it.toBlockKitManage() }
        var makeString = ""
        updateFormat.block.forEachIndexed { index, blockKitManage ->
            blockList = blockList.mapIndexed { i, b ->
                if (i == index) {
                    b.copy(
                        startIndex = makeString.length,
                        endIndex = makeString.length + b.word.length
                    )
                } else {
                    b
                }
            }
            makeString += blockKitManage.word
        }
        text = makeString
        return blockList
    }
}