package com.example.markdownapp.utils

import android.text.SpannableStringBuilder
import android.widget.EditText
import com.example.markdownapp.spanrichlib.MentionDataClass
import com.example.markdownapp.spanrichlib.StyleMakeSpan

object JsonBlock {


    fun EditText.toConvertJsonBlock(){

        val jsonBlock = mutableListOf<MentionDataClass>()

        val spannableString = SpannableStringBuilder(text)

        var currentIndex = 0

        text.split("\\s+".toRegex()).forEachIndexed { index, s ->

            val startIndex = text.indexOf(s,currentIndex)

            currentIndex = startIndex+s.length

            val spanList = spannableString.getSpans(startIndex,currentIndex,StyleMakeSpan::class.java)
            spanList.forEach {
                val start = spannableString.getSpanStart(it)
                val end = spannableString.getSpanEnd(it)
                val original = spannableString.substring(start,end)
                println("JsonBlock EditText.toConvertJsonBlock() >>>>>> styles currentIndex$start startIndex $end >>>> s$original")
            }


            println("JsonBlock EditText.toConvertJsonBlock() >>>>>>currentIndex$startIndex startIndex $currentIndex >>>> s$s")
        }
    }


}