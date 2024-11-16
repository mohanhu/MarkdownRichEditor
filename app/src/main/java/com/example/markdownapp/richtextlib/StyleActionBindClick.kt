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

    fun EditText.addBulletList() {
        val start = selectionStart.takeIf { it != -1 } ?: 0
        val end = selectionEnd.takeIf { it != -1 } ?: 0
        val selectedText = text.substring(start, end)

        // Split the selected text into lines
        val lines = selectedText.split("\n")

//        Check the starting style is bullet
        val isBulletStyle = lines.first().trimStart().startsWith("•")

        // Check if the cursor is at the start of a new line
        val isAtLineStart = start == 0 || text[start - 1] == '\n'

        // Toggle bullets: If a line starts with a bullet, remove it; otherwise, add it
        val bulletListText = if (isAtLineStart) {
            lines.joinToString("\n") { line ->
                if (line.trimStart().startsWith("•") && isBulletStyle) {
                    // Remove the bullet if it starts with "•"
                    line.trimStart().removePrefix("• ").trimStart()
                } else {
                    // Add a bullet if it doesn't start with "•"
                    "• $line"
                }
            }
        } else {
            "\n" + lines.joinToString("\n") { line ->
                if (line.trimStart().startsWith("•") && isBulletStyle) {
                    // Remove the bullet if it starts with "•"
                    line.trimStart().removePrefix("• ").trimStart()
                } else {
                    // Add a bullet if it doesn't start with "•"
                    "• $line"
                }
            }
        }

        // Replace the selected text with the modified bullet list
        text.replace(start, end, bulletListText)

        // Adjust the cursor position after inserting or removing bullets
        setSelection(start + bulletListText.length)
    }

    fun EditText.addNumberList() {
        val start = selectionStart.takeIf { it != -1 } ?: 0
        val end = selectionEnd.takeIf { it != -1 } ?: 0
        val selectedText = text.substring(start, end)

        val lines = selectedText.split("\n")

        //    Check the starting style is Number
        println("fun EditText.addNumberList() >>> ${lines.first().trimStart().indexOfFirst { it.isDigit() }}")
        val isNumberStyle = lines.first().trimStart().indexOfFirst { it.isDigit() }==0

        val isAtLineStart = start == 0 || text[start - 1] == '\n'

        // Toggle numbering: If a line starts with a number followed by a period, remove it; otherwise, add numbering
        val numberListText = if (isAtLineStart) {
            lines.mapIndexed { index, line ->
                val trimmedLine = line.trimStart()
                val regex = Regex("""^\d+\.\s""")  // Regex to check if the line starts with a number and a period (e.g., "1. ")
                if (regex.containsMatchIn(trimmedLine) && isNumberStyle) {
                    // Remove the numbering (e.g., "1. ") if it starts with a number
                    trimmedLine.replaceFirst(regex, "").trimStart()
                } else {
                    // Add numbering if the line does not start with a number
                    "${index + 1}. $line"
                }
            }.joinToString("\n")
        } else {
            "\n" + lines.mapIndexed { index, line ->
                val trimmedLine = line.trimStart()
                val regex = Regex("""^\d+\.\s""")

                if (regex.containsMatchIn(trimmedLine) && isNumberStyle) {
                    // Remove the numbering
                    trimmedLine.replaceFirst(regex, "").trimStart()
                } else {
                    "${index + 1}. $line"
                }
            }.joinToString("\n")
        }

        // Replace the selected text with the toggled numbered list
        text.replace(start, end, numberListText)

        // Adjust the cursor position after modifying the list
        setSelection(start + numberListText.length)
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
            println("currentLineStartsWithDash checkEmpty ${textBeforeCursor.substring(lastBullet,cursorPos).trimIndent().isEmpty()}")
            textBeforeCursor.substring(lastBullet,cursorPos).trimIndent().isNotEmpty()
        }
        else {
            false
        }
    }

    fun EditText.currentLineStartsWithNumberedList(): Pair<Boolean,Int> {
        val cursorPos = selectionStart.takeIf { it >= 0 } ?: 0
        println("currentLineStartsWithNumberedList cursorPos <<<< $cursorPos")
        val textBeforeCursor = text.toString().substring(0, cursorPos)

        if (!textBeforeCursor.endsWith("\n"))
            return Pair(false,0)

        // Get the current line
        val lines = textBeforeCursor.trimIndent().split("\n")
        val currentLine = lines.lastOrNull() ?: ""

        // Check if the line starts with a number followed by a dot (e.g., "1. ")
        val regex = Regex("""^\d+\.\s""")

        return if (regex.containsMatchIn(currentLine.trimStart())) {
            // Find the last numbered list occurrence (e.g., "1. ", "2. ", etc.)
            val lastNumberedIndex = textBeforeCursor.indexOfLast { it.isDigit() }+2
            // Ensure there's at least one space after the number and check if the remaining part is not empty
            println("currentLineStartsWithNumberedList checkEmpty ${textBeforeCursor.substring(lastNumberedIndex, cursorPos).trimIndent()}")

            val isCharHaveSomething = textBeforeCursor.substring(lastNumberedIndex, cursorPos).trimIndent().isNotEmpty()

            val matchResult = regex.find(currentLine.trimStart())
            println("currentLineStartsWithNumberedList matchResult ${matchResult?.groupValues?.get(0)?.replace(".","")?.replace(Regex("[.\\s]"), "")?.toInt()}")
            val number = matchResult?.groupValues?.get(0)?.replace(".","")?.replace(Regex("[.\\s]"), "")?.toIntOrNull()?:0
            println("currentLineStartsWithNumberedList number $number")
            Pair(isCharHaveSomething,number+1)
        } else {
            Pair(false,0)
        }
    }

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