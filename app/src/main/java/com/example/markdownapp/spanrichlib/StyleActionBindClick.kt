package com.example.markdownapp.spanrichlib

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.markdownapp.R
import com.example.markdownapp.utils.safeCall

object StyleActionBindClick {

    fun EditText.addBulletList() {
        val spannableStringBuilder = SpannableStringBuilder(text)
        val start = selectionStart.takeIf { it!=-1 }?:0
        val end = selectionEnd.takeIf { it!=-1 }?:0
        val selectedText = text.substring(start, end)

        val spannableBold = spannableStringBuilder.getSpans(start,end,StyleMakeSpan::class.java)
        val mentionSpan = spannableStringBuilder.getSpans(start,end,MentionClickableSpan::class.java)

        if (mentionSpan.isNotEmpty()) return
        spannableBold.forEach {
            if (it.style == Typeface.BOLD || it.style ==  Typeface.ITALIC){
                return
            }
        }

        // Split the selected text into lines
        val lines = selectedText.split("\n")

        // Check if the cursor is at the start of a new line
        val isAtLineStart = start == 0 || text[start - 1] == '\n'

        // If cursor is at the start of a line, add a newline character before the bullet
        val bulletListText = if (isAtLineStart) {
            lines.joinToString("\n") { "• $it" }
        } else {
            "\n"+  lines.joinToString("\n") { "• $it" }
        }
        // Replace the selected text with the bullet list
        spannableStringBuilder.replace(start, end, bulletListText)

        setText(spannableStringBuilder,TextView.BufferType.SPANNABLE)
        // Adjust the cursor position after inserting the bullet list
        setSelection(start + bulletListText.length)
    }

    fun EditText.currentLineStartsWithDash(): Boolean {
        val cursorPos = selectionStart?.takeIf { it>=0 }?:0
        println("currentLineStartsWithDash 0 <<<<$cursorPos")
        val textBeforeCursor = text.toString().substring(0, cursorPos)

        if (!textBeforeCursor.endsWith("\n"))
            return false
        // Get the current line
        val lines = textBeforeCursor.trimIndent().split("\n")
        val currentLine = lines.lastOrNull() ?: ""

        return if (currentLine.trimStart().startsWith('•')){
            val lastBullet = textBeforeCursor.indexOfLast { it == '•' }+1
            println("hjkebda kwfdygyut3uruat checkEmpty ${textBeforeCursor.substring(lastBullet,cursorPos).trimIndent().isEmpty()}")
            textBeforeCursor.substring(lastBullet,cursorPos).trimIndent().isNotEmpty()
        }
        else {
            false
        }
    }

    fun EditText.addMention(mention: String, mentionId: String, index:(Int, Int)->Unit) {
     safeCall {
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
        val selection = "uybrcfuyrwt3yuyvirqy3li"

        val spanAny = spannable.getSpans(start,end,Any::class.java)
        spanAny.forEach {
            spannable.removeSpan(it)
        }
        spannable.replace(start,end,selection)
        val clickableSpan = MentionClickableSpan(selection,url) { name,Id->
            println("MentionClickableSpan >>> $name <$Id")
        }
        clickableSpan.setCurrentStyle(Styles.LINK)
        spannable.setSpan(clickableSpan, start, start + selection.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.reply_message_sender_color)), start, start + selection.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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


