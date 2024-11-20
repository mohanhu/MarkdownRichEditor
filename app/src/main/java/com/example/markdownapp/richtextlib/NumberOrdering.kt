package com.example.markdownapp.richtextlib

import android.widget.EditText

object NumberOrdering {

    /**------------------------Forward--------------------------*/

    /**
     * Forward cursor Check above line has Number format
     **/
    private fun EditText.checkBeforeLineHaveNumber(cursor: Int) : Pair<Boolean,Int> {
        val textBeforeCursor = text.toString().substring(0, cursor)
        if (!textBeforeCursor.endsWith("\n"))
             return Pair(false,0)

        val regex = Regex("""^\d+\.\s""")

        val lines = textBeforeCursor.trimIndent().split("\n")
        val currentLine = lines.lastOrNull() ?: ""

        return if(regex.containsMatchIn(currentLine.trimStart())){
            val indexOfStart = currentLine.indexOfFirst { it.isDigit() }+2
            val number = findNumberForRespectiveString(currentLine.trimIndent())
            Pair(currentLine.substring(indexOfStart).trimIndent().isNotBlank(),number)
        }
        else{
            Pair(false,0)
        }
    }

    /**
     * Forward Number ordering mapping
     **/
    fun EditText.formatNumberForward(cursor:Int) {
        val checkIsStyleFormat = checkBeforeLineHaveNumber(cursor)
        if (checkIsStyleFormat.first){
            post {

                text?.insert(cursor, "${checkIsStyleFormat.second+1}. ")
                val end = text.toString().length
                val selectionText = text?.substring(cursor,end)?:""

                val lines = selectionText.trimIndent().split("\n")
                val regex = Regex("""^\d+\.\s""")

                var updateCountNumber = checkIsStyleFormat.second+1
                var currentIndex = cursor //3

                println("EditText.formatNumberStyles >>> number>${selectionText?.replace("\n"," ")}")
                println("EditText.formatNumberStyles >>> number>${lines}")

                lines.mapIndexed { index, line ->
                    if (index==0) {
                        // current line string add empty string length and \n length
                        currentIndex+=line.length+1
                        return@mapIndexed
                    }
                    if (regex.containsMatchIn(line.trimStart())) {
                        text?.replace(currentIndex,currentIndex+updateCountNumber.toString().length,(updateCountNumber+1).toString())
                        println("EditText.formatNumberBackward 2.0>>> forward >>${((updateCountNumber+1)%10==0)}")
                        val updateCount = if ((updateCountNumber+1)%10==0) 1 else 0
                        currentIndex+=line.length+updateCount+1
                        updateCountNumber++
                    }
                    else{
                        return@post
                    }
                }
            }
        }
    }

    /**------------------------Backward--------------------------*/

    /**
     * Backward cursor Check above line has Number format
     **/

    fun EditText.formatNumberBackward(cursor: Int) {
        val checkIsStyleFormat = checkNextLineHaveNumber(cursor)
        if(checkIsStyleFormat.first){
            post {
                val beforeCursorString = text?.substring(0,cursor)?.trimIndent()?.split("\n")?.last()
                val number = findNumberForRespectiveString(beforeCursorString?:"".trimStart())
                println("EditText.formatNumberBackward 2.0>>> number >>>$number")

                var updateCountNumber = number
                val end = text.toString().length

                val selectionText = text?.substring(cursor,end)?:""

                val lines = selectionText.trimIndent().split("\n")
                val regex = Regex("""^\d+\.\s""")

                var currentIndex = cursor+1

                println("EditText.formatNumberBackward 2.0>>> selectionText>${selectionText?.replace("\n"," ")}")
                println("EditText.formatNumberBackward 2.0>>> lines>${lines}")

                lines.mapIndexed { index, line ->
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
    }

    /**
     * Backward Next Line have number
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
            val indexOfStart = currentLine.indexOfFirst { it.isDigit() }+2
            val number = findNumberForRespectiveString(currentLine.trimIndent())
            Pair(currentLine.substring(indexOfStart).trimIndent().isNotBlank(),number)
        }
        else {
            Pair(false,0)
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