package com.example.markdownapp.richtextlib

import android.widget.EditText
import android.widget.TextView
import com.example.markdownapp.richtextlib.RichSpanDownStyle.makeStyleFormat
import com.example.markdownapp.richtextlib.StyleActionBindClick.editAddLink
import com.example.markdownapp.richtextlib.StyleActionBindClick.editAddMention
import com.google.gson.GsonBuilder

object BlockExport {

    fun EditText.blockJsonExportToEdit(blockJson: String){
       safeCall {
           val blockKitData = GsonBuilder().disableInnerClassSerialization()
               .disableHtmlEscaping().create()
               .fromJson(blockJson, BlockKitData::class.java)

           var blockKitManage = blockKitData.block?.map { it.toBlockKitManage() }
           var makeString = ""
           blockKitData.block?.mapIndexed { index, head ->
               blockKitManage = blockKitManage?.mapIndexed { i, b ->
                   if (i == index) {
                       b.copy(startIndex = makeString.length, endIndex = makeString.length + b.word.length)
                   } else {
                       b
                   }
               }
               makeString += head.text
           }
           if (makeString.trim().isNotEmpty()){
               setText(makeString, TextView.BufferType.SPANNABLE)
           }
           blockKitManage?.sortedBy { it.startIndex }?.forEach {
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
    }

    fun TextView.blockJsonToExportDataTextView(blockJson: String): List<MentionDataClass> {
        return try {
            println("TextView.blockJsonToExportDataTextView json >>> $blockJson")
//                val startTime = System.currentTimeMillis()
            val updateFormat = GsonBuilder().disableInnerClassSerialization()
                .disableHtmlEscaping().create().fromJson(blockJson, BlockKitData::class.java)
//                val endTime = System.currentTimeMillis()

//                println("blockJsonToExportDataTextView Deserialization took ${endTime - startTime} ms")
             var blockList = updateFormat.block?.map { it.toBlockKitManage() }
            var makeString = ""

            updateFormat.block?.mapIndexed { index, blockKitManage ->
                blockList = blockList?.mapIndexed { i, b ->
                    if (i == index) {
                        b.copy(
                            startIndex = makeString.length,
                            endIndex = makeString.length + b.word.length
                        )
                    } else {
                        b
                    }
                }
                makeString += blockKitManage.text
            }
            if (makeString.isNotEmpty()) {
                text = makeString // Update UI on the main thread
            }
            println("TextView.blockJsonToExportDataTextView >>>> $makeString")
            println("TextView.blockJsonToExportDataTextView >>>> $blockList")

            blockList?: listOf() // Return the list of MentionDataClass
        } catch (e: Exception) {
            println("Exception in blockJsonToExportDataTextView: ${e.message}")
            listOf() // Return an empty list on error
    }
}

    fun exportJsonToBlockDataClass(json:String): BlockKitData {
        return try {
            val updateFormat = GsonBuilder().disableInnerClassSerialization()
                .disableHtmlEscaping().create()
                .fromJson(json, BlockKitData::class.java)
            updateFormat
        }
        catch (e:Exception){
            BlockKitData()
        }
    }
}

fun safeCall(action:()->Unit){
    try {
        action()
    }
    catch (e:Exception){}
}