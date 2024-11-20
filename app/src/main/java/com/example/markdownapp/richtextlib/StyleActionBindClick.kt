package com.example.markdownapp.richtextlib

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.markdownapp.R

object StyleActionBindClick {

    fun EditText.addMention(mention: String, mentionId: String, index:(Int, Int)->Unit) {
        val start = selectionStart.takeIf { it>=0 }?:0
        val mentionWithSpaces = "@$mention"
        val spannable = SpannableStringBuilder(text)
        spannable.insert(start, mentionWithSpaces)

        val clickableSpan = MentionClickableSpan(mentionWithSpaces, mentionId) { name, Id->
            println("MentionClickableSpan >>> $name <$Id")
        }
        println("EditText.onTypeStateChange >>0> $start >>>${spannable.substring(start,start+mentionWithSpaces.length)}")

        clickableSpan.setCurrentStyle(Styles.MENTION)
        spannable.setSpan(clickableSpan, start, start + mentionWithSpaces.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.golden_yellow)), start, start + mentionWithSpaces.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(BackgroundColorSpan(ContextCompat.getColor(context, R.color.yellow_700a30)), start, start + mentionWithSpaces.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spans = spannable.getSpans(start,start+mentionWithSpaces.length,StyleMakeSpan::class.java)
        spans.forEach {
            spannable.removeSpan(it)
        }
        setText(spannable, TextView.BufferType.SPANNABLE)
        movementMethod = LinkMovementMethod.getInstance()
        text.insert(start + mentionWithSpaces.length," ")
        setSelection(start + mentionWithSpaces.length+1)
    }

    fun EditText.editAddMention(mention: String,mentionId:Long,start:Int,end:Int) {
        val spannable = SpannableStringBuilder(text as Spannable)

        val spans = spannable.getSpans(start,end,Any::class.java)
        spans.forEach {
            spannable.removeSpan(it)
        }

        val clickableSpan = MentionClickableSpan(mention,mentionId.toString()) { name,Id->
            println("MentionClickableSpan >>> $name <$Id")
        }
        clickableSpan.setCurrentStyle(Styles.MENTION)
        spannable.setSpan(clickableSpan, start,
            start + mention.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.golden_yellow)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(BackgroundColorSpan(ContextCompat.getColor(context, R.color.yellow_700a30)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setText(spannable, TextView.BufferType.SPANNABLE)
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun EditText.addLinkMarkdown(url: String) {
        val start = selectionStart.takeIf { it != -1 } ?: 0
        val end = selectionEnd.takeIf { it != -1 } ?: 0
        val spannable = SpannableStringBuilder(text)
        val selection = text.toString().substring(start,end)

        val clickableSpan = MentionClickableSpan(selection,url) { name,Id->
            println("MentionClickableSpan >>> $name <$Id")
        }
        clickableSpan.setCurrentStyle(Styles.LINK)
        spannable.setSpan(clickableSpan, start, start + selection.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.reply_message_sender_color)), start, start + selection.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spans = spannable.getSpans(start,start+selection.length,StyleMakeSpan::class.java)
        spans.forEach {
            spannable.removeSpan(it)
        }
        setText(spannable, TextView.BufferType.SPANNABLE)
        text?.insert(start+selection.length," ")
        setSelection(start+selection.length+1)
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun EditText.editAddLink(name: String,url:String,start:Int,end:Int) {
        val spannable = SpannableStringBuilder(text as Spannable)

        println("MentionClickableSpan >>>  $start$end")
        println("MentionClickableSpan >>>  <$name>$url>${text.substring(start,end)}")

        val spans = spannable.getSpans(start,end,Any::class.java)
        spans.forEach {
            spannable.removeSpan(it)
        }

        val clickableSpan = MentionClickableSpan(name, url) { _, Id->
        }
        clickableSpan.setCurrentStyle(Styles.LINK)
        spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.reply_message_sender_color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setText(spannable, TextView.BufferType.SPANNABLE)
        movementMethod = LinkMovementMethod.getInstance()
    }
}