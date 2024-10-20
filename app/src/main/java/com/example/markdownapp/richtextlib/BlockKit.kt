package com.example.markdownapp.richtextlib

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.widget.EditText
import com.google.gson.annotations.SerializedName

object BlockKit {

    fun EditText.blockKitListGenerate() : BlockKitData {

        val spannableText = SpannableStringBuilder(text)

        /**
         * Filter based on MentionClickableSpan to filter mention and link
         * */

        val mentionSpans = spannableText.getSpans(0, spannableText.length, MentionClickableSpan::class.java)
        val mentionList = mentionSpans.sortedBy { spannableText.getSpanStart(it) }.map { span ->
            val start = spannableText.getSpanStart(span)
            val end = spannableText.getSpanEnd(span)
            val original = if (span.getCurrentStyle()==Styles.MENTION){
                spannableText.substring(start,end)
            }
            else{
                spannableText.substring(start,end)
            }
            val spanOrigin = span.getMentionName()

            val pattern = when(span.getCurrentStyle()){
                Styles.MENTION -> "<@${span.getMentionId()},${span.getMentionName()}>"
                Styles.LINK -> "[${span.getMentionId()}](${span.getMentionName()})"
                else->""
            }
            println("EditText.blockKitListGenerate()  >>> $pattern")
            println("EditText.blockKitListGenerate()  >>>$original>$spanOrigin>")
            MentionDataClass(startIndex = start, endIndex = end , isSame = original == spanOrigin, pattern = pattern, word = span.getMentionName(), key = span.getMentionId(), styleFormat = span.getCurrentStyle())
        }.toMutableList()

        /**
         * Filter based on StyleMakeSpan to filter bold , italic & if want ,will add
         * */

        val boldSpan = spannableText.getSpans(0 , spannableText.length, StyleMakeSpan::class.java)
        boldSpan.toList().asSequence().filter { span -> span.style == Typeface.BOLD || span.style== Typeface.ITALIC }
            .map { span->
                val start = spannableText.getSpanStart(span)
                val end = spannableText.getSpanEnd(span)
                val styleFormat = when(span.style){
                    Typeface.ITALIC -> Styles.ITALIC
                    Typeface.BOLD -> Styles.BOLD
                    else -> Styles.PLAIN
                }
                val original = spannableText.substring(start,end)
                println("Mohan Typeface.BOLD::class.java ::: getChangedName>start>$start>end>$end >start>$original>end>${span.getChangedName()}>style>$styleFormat")
                MentionDataClass(startIndex = start, endIndex = end , isSame = true ,word = original , key = "", styleFormat = styleFormat )
            }.toList()
            .reversed().distinctBy { Pair(it.startIndex, it.endIndex) }.sortedBy { it.startIndex }
            .also { dataClasses ->
                println("Mohan Typeface.BOLD::class.java ::: getChangedName>start>filter>>>$dataClasses")
            mergeAdjacentMentions(dataClasses,text.toString()).also { update->
                mentionList.addAll(update)
            }
        }

        val filterList = filterBasedOnIndex(mentionList.filter { it.isSame }.sortedBy { it.startIndex })

        return BlockKitData(text = "", block = filterList.map { it.toBlockKitManage() } )
    }

    /**
     *  Merge item if continuous word iteration but split by item to group
     *  And to group word in between styles ( One style hide to another style )
     * */

    private fun mergeAdjacentMentions(mentions: List<MentionDataClass>, word: String): List<MentionDataClass> {
        if (mentions.isEmpty()) return emptyList()

        val mergedMentions = mutableListOf<MentionDataClass>()
        val lastFilter = mutableListOf<MentionDataClass>()

        var current = mentions[0]

        for (next in mentions.drop(1)) {
            if (current.endIndex == next.startIndex && current.styleFormat == next.styleFormat) {
                // Merge the two entries
                current = current.copy(endIndex = next.endIndex, word = current.word + next.word)
            } else {
                // Add the current entry to the list and move to the next
                mergedMentions.add(current)
                current = next
            }
        }
        // Add the last entry
        mergedMentions.add(current)

        println("mergeAdjacentMentions  <<<parent $mergedMentions")

        if (mergedMentions.size>1){
            mergedMentions.forEachIndexed { index, mentionDataClass ->
            try {
                val first = mergedMentions[index]
                val next = mergedMentions[index+1]
                println("mergeAdjacentMentions  <<<first $first")
                println("mergeAdjacentMentions  <<<next $next")
                if (first.endIndex > next.startIndex) {
                    println("mergeAdjacentMentions  <<<satisfied>> ")
                    lastFilter.add(MentionDataClass(startIndex = first.startIndex, endIndex = next.startIndex, word = word.substring(first.startIndex,next.startIndex), styleFormat = first.styleFormat))
                    lastFilter.add(MentionDataClass(startIndex = next.endIndex, endIndex = first.endIndex, word = word.substring(next.endIndex,first.endIndex), styleFormat = first.styleFormat))
                    lastFilter.add(next)
                }
                else{
                    lastFilter.add(first)
                    lastFilter.add(next)
                }
            }
            catch (e:Exception){
                lastFilter.add(mergedMentions.last())
            }
            }
        }
        else{
            lastFilter.addAll(mergedMentions)
        }
        println("mergeAdjacentMentions  <<<final ${lastFilter.toDistinctItemsToSort()}")
        return lastFilter.toDistinctItemsToSort()
    }

    /**
     *  Filter item of Plain text without style and order it using word like 0 to word.length order
     * */

    private fun EditText.filterBasedOnIndex(blockKitManages: List<MentionDataClass>): List<MentionDataClass> {
        var cursorStart = 0
        val remainsList = mutableListOf<MentionDataClass>()
        blockKitManages.forEach { b->
            if (cursorStart < b.startIndex){
                remainsList.add(MentionDataClass(startIndex = cursorStart, endIndex = b.startIndex, word = text.substring(cursorStart,b.startIndex), styleFormat = Styles.PLAIN))
            }
            cursorStart = b.endIndex
        }

        if (cursorStart<text.length){
            remainsList.add(MentionDataClass(startIndex = cursorStart, endIndex =text.length, word = text.substring(cursorStart), styleFormat = Styles.PLAIN))
        }
        remainsList.addAll(blockKitManages)
        return remainsList.toDistinctItemsToSort()
    }

    private fun List<MentionDataClass>.toDistinctItemsToSort() =
       distinctBy { it.startIndex }.distinctBy { Pair(it.startIndex,it.endIndex) }.sortedBy { it.startIndex }

}

data class BlockKitData (
    @SerializedName("text") val text : String = "",
    @SerializedName("block") val block: List<BlockKitManage> = listOf()
)

data class BlockKitManage(
    @SerializedName("word") val word:String = "",
    @SerializedName("key") val key : String="",
    @SerializedName("styleFormat") val styleFormat : Styles,
    @SerializedName("pattern") val pattern: String = ""
) {
    fun toBlockKitManage() = MentionDataClass (
        word = word,
        key = key,
        styleFormat = styleFormat,
        pattern = pattern
    )
}

data class MentionDataClass (
    val startIndex:Int = 0,
    val endIndex :Int= 0,
    val word: String = "",
    val key : String="",
    val pattern: String = "",
    val styleFormat : Styles,
    val isSame:Boolean = true
) {
    fun toBlockKitManage() = BlockKitManage(
        word = word,
        key = key,
        styleFormat = styleFormat,
        pattern = pattern
    )
}

enum class Styles{
    BOLD,
    ITALIC,
    MENTION,
    LINK,
    PLAIN
}