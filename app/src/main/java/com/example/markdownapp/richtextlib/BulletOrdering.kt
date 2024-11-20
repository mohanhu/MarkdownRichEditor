package com.example.markdownapp.richtextlib

import android.widget.EditText

object BulletOrdering {

    /**------------------------Bullet Style Ordering--------------------------*/

    fun EditText.addBulletList() {
        val start = selectionStart.takeIf { it != -1 } ?: 0
        val end = selectionEnd.takeIf { it != -1 } ?: 0
        val selectedText = text.substring(start, end)

        // Split the selected text into lines
        val lines = selectedText.split("\n")

//        Check the starting style is bullet
        val isBulletStyle = lines.first().trimStart().startsWith("•")

        var currentIndex = start

        // Check if the cursor is at the start of a new line
        val isAtLineStart = start == 0 || text[start - 1] == '\n'

        if (isBulletStyle){
            currentIndex+=2
            lines.forEachIndexed { index, s ->
                if (s.startsWith("•")) {
                    text?.delete(currentIndex-2,currentIndex)
                    currentIndex+=s.length-1
                }
                else{
                    currentIndex+=s.length
                }
            }
        }
        else{
            if (isAtLineStart){
                lines.forEachIndexed { index, s ->
                    text?.insert(currentIndex,"• ")
                    currentIndex += s.length+3
                }
            }
            else{
                text?.insert(currentIndex,"\n")
                currentIndex++
                lines.forEachIndexed { index, s ->
                    text?.insert(currentIndex,"• ")
                    currentIndex += s.length+3
                }
            }
        }

        /**
         * Avoid other style remove issue
         * */

//        // Toggle bullets: If a line starts with a bullet, remove it; otherwise, add it
//        val bulletListText = if (isAtLineStart) {
//            lines.joinToString("\n") { line ->
//                if (line.trimStart().startsWith("•") && isBulletStyle) {
//                    // Remove the bullet if it starts with "•"
//                    line.trimStart().removePrefix("• ").trimStart()
//                } else {
//                    // Add a bullet if it doesn't start with "•"
//                    "• $line"
//                }
//            }
//        } else {
//            "\n" + lines.joinToString("\n") { line ->
//                if (line.trimStart().startsWith("•") && isBulletStyle) {
//                    // Remove the bullet if it starts with "•"
//                    line.trimStart().removePrefix("• ").trimStart()
//                } else {
//                    // Add a bullet if it doesn't start with "•"
//                    "• $line"
//                }
//            }
//        }

//        // Replace the selected text with the modified bullet list
//        text.replace(start, end, bulletListText)
//
//        // Adjust the cursor position after inserting or removing bullets
//        setSelection(start + bulletListText.length)
    }

    /**------------------------Bullet Style Forward--------------------------*/

    fun EditText.bulletFormatForward(cursor:Int) {
        val cursorPos = selectionStart?.takeIf { it>=0 }?:0
        println("currentLineStartsWithDash 0 <<<<$cursorPos")
        val textBeforeCursor = text.toString().substring(0, cursorPos)

        if (!textBeforeCursor.endsWith("\n"))
            return
        // Get the current line
        val lines = textBeforeCursor.trimIndent().split("\n")
        val currentLine = lines.lastOrNull() ?: ""

        if (currentLine.trimStart().startsWith('•')){
            val lastBullet = currentLine.indexOfLast { it == '•' }+1
            println("currentLineStartsWithDash checkEmpty ${currentLine.substring(lastBullet).trimIndent().isEmpty()}")
            if(currentLine.substring(lastBullet).trimIndent().isNotEmpty()){
                text?.insert(cursor, "• ")
            }
        }
    }
}