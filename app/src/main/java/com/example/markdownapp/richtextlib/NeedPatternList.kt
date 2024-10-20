package com.example.markdownapp.richtextlib

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NeedPatternList(
    val neededType: PATTERN_TYPE,
    val color:Int
)

suspend fun needPatternList(input:String, needPatternList: List<NeedPatternList>):List<PatternString> {

    return withContext(Dispatchers.IO) {

        val patternString = mutableListOf<PatternString>()

        needPatternList.map {
            when(it.neededType){
                PATTERN_TYPE.MENTION -> {
                    val userMentionPattern = Regex(it.neededType.pattern)
                    (userMentionPattern.findAll(input)).forEachIndexed { index, matchResult ->
                        addToList(matchResult,patternString,it)
                    }
                }
                PATTERN_TYPE.URL_PATTERN -> {
                    val userMentionPattern = Regex(it.neededType.pattern)
                    (userMentionPattern.findAll(input)).forEachIndexed { index, matchResult ->
                        addToList(matchResult,patternString,it)
                    }
                }
            }
        }
        patternString
    }
}

fun addToList(
    matchResult: MatchResult,
    patternString: MutableList<PatternString>,
    pattern: NeedPatternList
) {
    patternString.add(
        PatternString(
        patternValue = matchResult.value,
        patternType = pattern.neededType,
        start = matchResult.range.first,
        end = matchResult.range.last,
        color = pattern.color
    )
    )
}

private val Context.inputManager: InputMethodManager get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

fun EditText.hideKeyBoard (){
    post{
        requestFocus()
        context.inputManager.hideSoftInputFromWindow(windowToken,0)
    }
}