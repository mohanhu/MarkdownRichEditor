package com.example.markdownapp.richtextlib

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.EditText
import android.widget.TextView

object RichSpanDownStyle {

    fun EditText.toggleStyle(style : Styles) {

        val styleType = when(style){
            Styles.BOLD -> Typeface.BOLD
            Styles.ITALIC -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }

        val start = selectionStart.takeIf { it>=0 }?:0
        val end = selectionEnd.takeIf { it>=0 }?:0
        val selectorTextAndUpdate = text.substring(start,end)
        val spannableText = text as Spannable

        val spans = spannableText.getSpans(start, end, StyleMakeSpan::class.java)
        val mentionLink = spannableText.getSpans(start, end, MentionClickableSpan::class.java)

        if (selectorTextAndUpdate.isEmpty() && start==end ){
            text?.insert(start,"\u00A0")
            spannableText.setSpan(StyleMakeSpan(styleType,"\u00A0"),start,start+1,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSelection(start,start+1)
            return
        }
        mentionLink.forEach { _ ->
            return
        }

        var isStyle = false

        spans.forEach { span ->
            if (span.style == styleType) {
                isStyle = true
            }
            spannableText.removeSpan(span) // Remove existing style span
        }

        // If the text is not style, apply the span
        if (!isStyle) {
            spannableText.setSpan(StyleMakeSpan(styleType,selectorTextAndUpdate), start, start+selectorTextAndUpdate.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        setSelection(start+selectorTextAndUpdate.length)
    }


    fun EditText.makeStyleFormat(mention: String, style: Styles, start:Int, end:Int) {
        val spannable = text as Spannable
        when(style){
            Styles.BOLD -> spannable.setSpan(StyleMakeSpan(Typeface.BOLD,mention), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            Styles.ITALIC -> spannable.setSpan(StyleMakeSpan(Typeface.ITALIC,mention), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            else -> {}
        }
//        setText(spannable, TextView.BufferType.SPANNABLE)
//        setSelection(start + mention.length)
    }

    @Deprecated("Initially not in use")
    fun EditText.handleInBetweenStylesChar(start: Int, count: Int, currentTypeStyle: Styles) {
        try {
            // Use Editable directly to avoid recreating Spannable unnecessarily
            val editableText = text as Spannable

            val style = when (currentTypeStyle) {
                Styles.BOLD -> Typeface.BOLD
                Styles.ITALIC -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }

            // Get spans at the affected range
            val styleSpans = editableText.getSpans(start, start + count, StyleMakeSpan::class.java)

            // Process each span in the affected range
            styleSpans.map { span ->
                val spanStart = editableText.getSpanStart(span)
                val spanEnd = editableText.getSpanEnd(span)

                if (start in spanStart..spanEnd) {

                    if (span.style!=style){
                        return@map
                    }

                    // Ensure we're not inside a MentionClickableSpan
                    val mentionClickableSpans = editableText.getSpans(spanStart, spanEnd, MentionClickableSpan::class.java)
                    if (mentionClickableSpans.isNotEmpty()) {
                        return
                    }

                    // Determine the style and apply only if needed
                    when (span.style) {
                        Typeface.BOLD -> {
                            val latestText = editableText.substring(spanStart, spanEnd + count - 1)
                            editableText.removeSpan(span)
                            editableText.setSpan(StyleMakeSpan(Typeface.BOLD, latestText), spanStart, spanStart + latestText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        Typeface.ITALIC -> {
                            val latestText = editableText.substring(spanStart, spanEnd + count - 1)
                            editableText.removeSpan(span)
                            editableText.setSpan(StyleMakeSpan(Typeface.BOLD, latestText), spanStart, spanStart + latestText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        Typeface.NORMAL -> {
                            return // No need to process normal text
                        }
                    }
                }
            }
        }catch (e:Exception){}
    }

    fun EditText.removeAllSpans(){
        val spannable = SpannableStringBuilder(text)
        val spans = spannable.getSpans(0,text.length,Any::class.java)
        spans.forEach {
            spannable.removeSpan(it)
        }
        setText(spannable,TextView.BufferType.SPANNABLE)
        setText("")
    }

    fun TextView.removeAllSpans(){
        val spannable = SpannableStringBuilder(text)
        val spans = spannable.getSpans(0,text.length,Any::class.java)
        spans.forEach {
            spannable.removeSpan(it)
        }
        setText(spannable,TextView.BufferType.SPANNABLE)
    }


    fun EditText.onTypeStateChange(start: Int,count: Int, currentTypeStyle: Styles) {

        try {

            println("Updating span from setSpan(StyleMakeSpan >>start> $start >>count>$count >>currentTypeStyle>$currentTypeStyle")
            println("Updating span from .setSpan(StyleMakeSpan >>text > ${text.substring(start-count,start)}")

            val style = when (currentTypeStyle) {
                Styles.BOLD -> Typeface.BOLD
                Styles.ITALIC -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }

            val editableText = text as Spannable

            // Get existing spans in the current region
            val existingSpans = editableText.getSpans(start - count, start, StyleMakeSpan::class.java)
            val mentionSpans = editableText.getSpans(start - count, start, MentionClickableSpan::class.java)

            // Check if the current text already contains a mention span
            mentionSpans.map {
                if (it.getCurrentStyle() == Styles.LINK || it.getCurrentStyle() == Styles.MENTION) {
                    setSelection(start)
                    return
                }
            }

            println("EditText.onTypeStateChange >>> existingSpans>${existingSpans.size}")

            if (existingSpans.isEmpty()){
                editableText.setSpan(StyleMakeSpan(style, editableText.substring(start-count, start)), start-count, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                return
            }

            existingSpans.forEach {
                val startSpan = editableText.getSpanStart(it)
                val endSpan = editableText.getSpanEnd(it)
                if (start in startSpan..endSpan) {
                    if (it.style == style) {
                        editableText.removeSpan(it)
                        println("Updating span from yes $startSpan to $endSpan >>>$start")
                        if (startSpan < start) {
                            println("Updating span from yes case 1 passed ")
                            editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(startSpan,start)), startSpan, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        if (start < endSpan) {
                            println("Updating span from yes case 2 passed ")
                            editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(start,endSpan)), start, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    } else {
                        println("Updating span from no $startSpan to $endSpan >>>$start")
                        editableText.removeSpan(it)
                        if (startSpan < start-count) {
                            println("Updating span from no case 1 passed ")
                            editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(startSpan,start-count)), startSpan, start-count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        if (start < endSpan) {
                            println("Updating span from no case 2 passed ")
                            editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(start,endSpan)), start, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        editableText.setSpan(StyleMakeSpan(typeface = style,text.toString().substring(start-count, start)), start-count, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }}
            setSelection(start)
        }
        catch (e:Exception){

        }
    }

    fun EditText.listenCurrentStyleFormat(currentStyle: (Styles)->Unit) {

        val spannableString = text as Spannable

        val listOfSpans = spannableString.getSpans(selectionStart,selectionEnd, StyleMakeSpan::class.java)

        println("binding.overlayEditText.setOnTouchListener ACTION_UP >>0>${listOfSpans.size}")

        if (listOfSpans.isNotEmpty()){
            listOfSpans.map {
                val startOfSpan = spannableString.getSpanStart(it)
                val endOfSpan = spannableString.getSpanStart(it)
                println("binding.overlayEditText.setOnTouchListener ACTION_UP >>1>$startOfSpan >$selectionStart >>>$endOfSpan")
                when (it.style) {
                    Typeface.BOLD -> {
                        currentStyle.invoke(Styles.BOLD)
                    }
                    Typeface.ITALIC -> {
                        currentStyle.invoke(Styles.ITALIC)
                    }
                    else -> {
                        currentStyle.invoke(Styles.PLAIN)
                    }
                }
            }
        }
        else{
            currentStyle.invoke(Styles.PLAIN)
        }
    }
}
