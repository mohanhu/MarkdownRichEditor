package com.example.markdownapp.spanrichlib

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

        if (selectorTextAndUpdate.isEmpty() || start==end ){
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
        setSelection(start)
    }


    fun EditText.makeStyleFormat(mention: String,style:Styles,start:Int,end:Int) {
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


    fun EditText.onTypeStateChange(start: Int, currentTypeStyle: Styles) {

        println("editableText.setSpan(StyleMakeSpan >>> $start >>>$currentTypeStyle")

        val style = when (currentTypeStyle) {
            Styles.BOLD -> Typeface.BOLD
            Styles.ITALIC -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }

        val editableText = text as Spannable

        // Get existing spans in the current region
        val existingSpans = editableText.getSpans(start - 1, start, StyleMakeSpan::class.java)
        val mentionSpans = editableText.getSpans(start - 1, start, MentionClickableSpan::class.java)

        // Check if the current text already contains a mention span
        mentionSpans.map {
            if (it.getCurrentStyle() == Styles.LINK || it.getCurrentStyle() == Styles.MENTION) {
                setSelection(start)
                return
            }
        }

        println("EditText.onTypeStateChange >>> existingSpans>${existingSpans.size}")

       try {
           if (existingSpans.isEmpty()){
               editableText.setSpan(StyleMakeSpan(style, editableText.substring(start-1, start)), start-1, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
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
                           editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(startSpan,start)), startSpan, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                       }
                       if (start < endSpan) {
                           editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(startSpan,start)), start, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                       }
                   } else {
                       println("Updating span from $startSpan to $start")
                       editableText.removeSpan(it)
                       if (startSpan < start-1) {
                           editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(startSpan,start-1)), startSpan, start-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                       }
                       if (start < endSpan) {
                           editableText.setSpan(StyleMakeSpan(typeface = it.style,text.toString().substring(start,endSpan)), start, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                       }
                       editableText.setSpan(StyleMakeSpan(typeface = style,text.toString().substring(startSpan,start-1)), start-1, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                   }
               }}
           setSelection(start)
       }
       catch (e:Exception){

       }
    }

    fun EditText.listenCurrentStyleFormat(currentStyle: (Styles)->Unit) {

        val spannableString = text as Spannable

        val listOfSpans = spannableString.getSpans(selectionStart,selectionEnd,StyleMakeSpan::class.java)

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
