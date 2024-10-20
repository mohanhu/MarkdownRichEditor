package com.example.markdownapp.richtextlib

import java.util.regex.Pattern

data class PatternString (
    val patternValue : String = "",
    val start:Int = 0,
    val end:Int = 0,
    val color:Int = 0,
    val patternType : PATTERN_TYPE = PATTERN_TYPE.MENTION
)

enum class PATTERN_TYPE(val pattern : String) {
    MENTION(pattern = Pattern.compile("<@[^>]+>").toString()),
    URL_PATTERN(pattern =  Pattern.compile(
        "(^|[\\s.:;?\\-\\]<\\(])" +
                "((https?://|www\\.|pic\\.)[-\\w;/?:@&=+$\\|\\_.!~*\\|'()\\[\\]%#,☺]+[\\w/#](\\(\\))?)" +
                "(?=$|[\\s',\\|\\(\\).:;?\\-\\[\\]>\\)])").toString())
}