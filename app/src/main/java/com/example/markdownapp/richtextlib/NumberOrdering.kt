package com.example.markdownapp.richtextlib

import android.widget.EditText

object NumberOrdering {

    fun EditText.addNumberList() {
        val start = selectionStart.takeIf { it != -1 } ?: 0
        val end = selectionEnd.takeIf { it != -1 } ?: 0
        val selectedText = text.substring(start, end)

        val lines = selectedText.split("\n")

        //    Check the starting style is Number
        println("fun EditText.addNumberList() >>> ${lines.first().trimStart().indexOfFirst { it.isDigit() }}")
        val isNumberStyle = lines.first().trimStart().indexOfFirst { it.isDigit() }==0

        val isAtLineStart = start == 0 || text[start - 1] == '\n'

        var currentIndex = start

        val regex = Regex("""^\d+\.\s""")

        if (isNumberStyle){
            currentIndex+=3
            lines.forEachIndexed { index, s ->
                if (regex.containsMatchIn(s)) {
                    text?.delete(currentIndex-3,currentIndex)
                    currentIndex+=s.length-2
                }
                else{
                    currentIndex+=s.length
                }
            }
        }
        else{
            if (isAtLineStart){
                lines.forEachIndexed { index, s ->
                    text?.insert(currentIndex,"${index+1}. ")
                    currentIndex += s.length+4
                }
            }
            else{
                if (checkCurrentLineHaveNumber(start).first){
                    text?.insert(currentIndex,"\n")
                }
                else{
                    text?.insert(currentIndex,"\n")
                    currentIndex++
                    lines.forEachIndexed { index, s ->
                        text?.insert(currentIndex,"${index+1}. ")
                        currentIndex += s.length+4
                    }
                }
            }
        }
    }

//        // Toggle numbering: If a line starts with a number followed by a period, remove it; otherwise, add numbering
//        val numberListText = if (isAtLineStart) {
//            lines.mapIndexed { index, line ->
//                val trimmedLine = line.trimStart()
//                val regex = Regex("""^\d+\.\s""")  // Regex to check if the line starts with a number and a period (e.g., "1. ")
//                if (regex.containsMatchIn(trimmedLine) && isNumberStyle) {
//                    // Remove the numbering (e.g., "1. ") if it starts with a number
//                    trimmedLine.replaceFirst(regex, "").trimStart()
//                } else {
//                    // Add numbering if the line does not start with a number
//                    "${index + 1}. $line"
//                }
//            }.joinToString("\n")
//        } else {
//            "\n" + lines.mapIndexed { index, line ->
//                val trimmedLine = line.trimStart()
//                val regex = Regex("""^\d+\.\s""")
//
//                if (regex.containsMatchIn(trimmedLine) && isNumberStyle) {
//                    // Remove the numbering
//                    trimmedLine.replaceFirst(regex, "").trimStart()
//                } else {
//                    "${index + 1}. $line"
//                }
//            }.joinToString("\n")
//        }
//
//        // Replace the selected text with the toggled numbered list
//        text.replace(start, end, numberListText)
//
//        // Adjust the cursor position after modifying the list
//        setSelection(start + numberListText.length)
    /**------------------------Forward--------------------------*/

    /**
     * Check cursor above line has Number format
     **/
    private fun EditText.checkBeforeLineHaveNumber(cursor: Int) : Pair<Boolean,Int> {
        val textBeforeCursor = text.toString().substring(0, cursor)
        val regex = Regex("""^\d+\.\s""")

        val lines = textBeforeCursor.trimIndent().split("\n")
        val currentLine = lines.lastOrNull() ?: ""

        if (!textBeforeCursor.endsWith("\n")){
            return Pair(false,0)
        }

        return if(regex.containsMatchIn(currentLine.trimStart())){
            val number = findNumberForRespectiveString(currentLine.trimIndent())
            val indexOfStart = currentLine.indexOfFirst { it.isDigit() }+number.toString().length+1
            Pair(currentLine.substring(indexOfStart).trimIndent().isNotBlank(),number)
        }
        else{
            Pair(false,0)
        }
    }

    /**
     * Check cursor current line has Number format
     **/
    private fun EditText.checkCurrentLineHaveNumber(cursor: Int) :Pair<Boolean,Int> {
        val regex = Regex("""^\d+\.\s""")
        val lastIndexOfNextLine = text?.substring(0,cursor).toString()?.lastIndexOf('\n')?.takeIf { it>0 }?:0
        val selectionText = text.toString().substring(lastIndexOfNextLine,cursor)
        val lines = selectionText.trimIndent().split("\n")
        val currentLine = lines.firstOrNull() ?: ""

        println("EditText.checkStartLineHaveNumber >>current>$currentLine>")
        return if (regex.containsMatchIn(currentLine.trimStart())) {
            val number = findNumberForRespectiveString(currentLine.trimIndent())
            val indexOfStart = currentLine.indexOfFirst { it.isDigit() }+number.toString().length+1
            println("EditText.checkStartLineHaveNumber >>number>$number")
            Pair(true,number)
        }
        else{
            Pair(false,0)
        }
    }

    /**
     * Check cursor below line has Number format
     **/

    private fun EditText.checkNextLineHaveNumber(cursor: Int): Pair<Boolean,Int> {
        val textBeforeCursor = text.toString().substring(cursor, text.toString().length)

        println("EditText.checkNextLineHaveNumber >>>>>$textBeforeCursor")

        if (!text.toString().substring(0,cursor).endsWith("\n") && cursor>0)
            return Pair(false,0)

        val regex = Regex("""^\d+\.\s""")

        val lines = textBeforeCursor.trimIndent().split("\n")
        val currentLine = lines.firstOrNull() ?: ""

        return if(regex.containsMatchIn(currentLine.trimStart())){
            val number = findNumberForRespectiveString(currentLine.trimIndent())
            val indexOfStart = currentLine.indexOfFirst { it.isDigit() }+number.toString().length+1
            Pair(currentLine.substring(indexOfStart).trimIndent().isNotBlank(),number)
        }
        else {
            Pair(false,0)
        }
    }

    /**
     * Forward Number increment by 1 if before line have number
     **/
    fun EditText.formatNumberForward(cursor:Int) {
        val checkIsStyleFormat = checkBeforeLineHaveNumber(cursor)
        println("EditText.formatNumberForward >>>$checkIsStyleFormat")
        if (checkIsStyleFormat.first){
            post {
                text?.insert(cursor, "${checkIsStyleFormat.second+1}. ")
            }
        }
        else{
//            val checkNextLineHaveNumber = checkNextLineHaveNumber(cursor)
//            if (checkNextLineHaveNumber.first){
//                post {
//                    text?.insert(cursor, "${checkNextLineHaveNumber.second}. ")
//                }
//            }
        }
    }

    /**
     * Check Start of current Line have number
     * */
    fun EditText.toFormatNumberBasedOnCursor(cursor: Int){

        val checkStartLineHaveNumber = checkCurrentLineHaveNumber(cursor)
        if (checkStartLineHaveNumber.first){
            formatNumber(cursor = cursor, number = checkStartLineHaveNumber.second)
            return
        }

        val checkWhileCursorInBeforeOfNewLine = checkBeforeLineHaveNumber(cursor)
        if (checkWhileCursorInBeforeOfNewLine.first){
            println("EditText.checkStartLineHaveNumber >>number>back>${checkWhileCursorInBeforeOfNewLine.second}")
            formatNumber(cursor,checkWhileCursorInBeforeOfNewLine.second)
            return
        }

        val checkNextLineHaveNumber = checkNextLineHaveNumber(cursor)
        if (checkNextLineHaveNumber.first){
            println("EditText.checkStartLineHaveNumber >>number>next>${checkWhileCursorInBeforeOfNewLine.second}")
            formatNumber(cursor,checkWhileCursorInBeforeOfNewLine.second)
            return
        }
    }

    /**
     * Format number from current line count to +1
     * */
    private fun EditText.formatNumber(cursor: Int, number: Int){
        post {
            var updateCountNumber = number
            val end = text.toString().length

            if (cursor>end) return@post

            println("EditText.formatNumber >>>>$cursor>$end")
            val selectionText = text?.substring(cursor,end)?:""

            val lines = selectionText.trimIndent().split("\n")
            val regex = Regex("""^\d+\.\s""")

            var currentIndex = cursor+1

            println("EditText.formatNumberBackward 2.0>>> selectionText>${selectionText?.replace("\n"," ")}")
            println("EditText.formatNumberBackward 2.0>>> lines>${lines}")

            lines.mapIndexed { index, line ->
                if (index==0 && !regex.containsMatchIn(line.trimStart())) {
                    currentIndex+=line.length
                    return@mapIndexed
                }
                if (regex.containsMatchIn(line.trimStart())) {
                    val nextLineNumber = findNumberForRespectiveString(line.trimStart())
                    if(updateCountNumber+1==nextLineNumber) return@post
                    println("EditText.formatNumberBackward 2.0>>> new lines>$nextLineNumber")
                    text?.replace(currentIndex,currentIndex+nextLineNumber.toString().length,(updateCountNumber+1).toString())
                    val spaceBetween = ((nextLineNumber.toString().length)-(updateCountNumber+1).toString().length)
                    currentIndex+=line.length+1-spaceBetween
                    updateCountNumber++
                }
                else{
                    return@post
                }
            }
        }
    }

    /**
     * To find number of starting in string
     * */
    private fun findNumberForRespectiveString(currentLine:String): Int {
        val regex = Regex("""^\d+\.\s""")
        val matchResult = regex.find(currentLine.trimStart())
        val group = matchResult?.groupValues?.get(0)?.replace(".","")
        val number = group?.replace(Regex("[.\\s]"), "")?.toIntOrNull()?:0
        return number
    }
}