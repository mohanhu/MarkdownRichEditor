package com.example.markdownapp.richtextlib

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.example.markdownapp.R
import com.example.markdownapp.extra.MarkDownCallBack
import com.example.markdownapp.extra.NeedPatternList
import com.example.markdownapp.extra.PATTERN_TYPE
import com.example.markdownapp.extra.PatternString
import com.example.markdownapp.extra.needPatternList
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.linkify.LinkifyPlugin

/**
* Text color conversion based on PATTERN_TYPE
**/

class RichSpanText(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context,attributeSet) {

    private lateinit var markDownCallBack: MarkDownCallBack

    fun getInstance(mark: MarkDownCallBack) {
        markDownCallBack = mark
    }

    private var spannableStringBuilder : SpannableStringBuilder = SpannableStringBuilder(" ")

    private val markwon = Markwon.builder(context)
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .build()

    override fun setText(text: CharSequence?, type: BufferType?) { super.setText(text, type) }

    suspend fun startPatternRecognition(
        markDown: Boolean,
        needPatternList: List<NeedPatternList>,
        mentionList: List<MentionDataClass>
    ) {
         /** This only for markdown after length issue soln if better get , will change */
        spannableStringBuilder = if (markDown){
            SpannableStringBuilder(markwon.toMarkdown(text.toString().replace("\n","  \n")+("\u00A0").repeat(50)))
        }
        else{
            SpannableStringBuilder(text.toString())
        }
        spannableStringBuilder = SpannableStringBuilder(spannableStringBuilder)

        val findString = needPatternList(spannableStringBuilder.toString(),needPatternList)
        if(findString.isNotEmpty()) {
            spanStringUpdate(findString){
                spanCustomStyle(mentionList)
            }
        }
        else{
            spanCustomStyle(mentionList)
        }
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = ContextCompat.getColor(context, R.color.transparent)
    }

    private suspend fun spanStringUpdate(
        findString: List<PatternString>,
        mentionFinish : ()->Unit
    ) {

//        try {
            findString.toList().asReversed().forEachIndexed { _, patternString ->
                var changeName = ""

                val start = patternString.start
                val end = patternString.end

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        when(patternString.patternType){
                            /**
                             * Temporarily get username through json .
                             * */
                            PATTERN_TYPE.MENTION -> {
                                val id = "<@(\\d+),([^>]+)>".toRegex()
                                val value = id.find(patternString.patternValue)?.groupValues
                                println("markDownCallBack.mentionOnClick >>> $value")
                                markDownCallBack.mentionOnClick(value?.get(1)?:"")
                            }
                            PATTERN_TYPE.URL_PATTERN->{
                               markDownCallBack.urlOnClick(patternString.patternValue)
                            }
                        }
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                    }
                }
                spannableStringBuilder.apply {
                    if (patternString.patternType== PATTERN_TYPE.MENTION){
                        val id = "<@(\\d+),([^>]+)>".toRegex()
                        val value = id.find(patternString.patternValue)?.groupValues
                        val userId = value?.get(1)?:0
                        changeName = value?.get(2)?:""

                        println("spanStringUpdate >>> start>$value>id<$userId>$changeName")
                        spannableStringBuilder = spannableStringBuilder.replace(start,end+1,changeName)
                        setSpan(clickableSpan, start, start + changeName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, patternString.color)), start, start + changeName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(BackgroundColorSpan(ContextCompat.getColor(context, R.color.yellow_700a30)), start, start + changeName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    else{
                        setSpan(clickableSpan, start, end+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, patternString.color)), start, end+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                setText(spannableStringBuilder,BufferType.SPANNABLE)
                spannableStringBuilder = SpannableStringBuilder(spannableStringBuilder)
            }
            mentionFinish.invoke()
//        }
//        catch (_:Exception){}
    }

    private fun spanCustomStyle(spans: List<MentionDataClass>){
//        try {
            spans.sortedBy { it.startIndex }.forEach {
                if(it.styleFormat == Styles.LINK){
                    val linkSpan = MentionClickableSpan(mentionName = it.word, mentionId = it.key){_,url->
                        markDownCallBack.urlOnClick(url = url)
                    }
                    linkSpan.setCurrentStyle(Styles.LINK)
                    spannableStringBuilder.setSpan(linkSpan,it.startIndex,it.endIndex,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableStringBuilder.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.reply_message_sender_color)), it.startIndex, it.startIndex+it.word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if ( it.styleFormat == Styles.MENTION) {
                    val linkSpan = MentionClickableSpan(mentionName = it.word, mentionId = it.key){_,url->
                        markDownCallBack.mentionOnClick(mentionId = url)
                    }
                    linkSpan.setCurrentStyle(Styles.MENTION)
                    spannableStringBuilder.setSpan(linkSpan,it.startIndex,it.endIndex,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableStringBuilder.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.golden_yellow)), it.startIndex, it.startIndex+it.word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableStringBuilder.setSpan(BackgroundColorSpan(ContextCompat.getColor(context, R.color.backdrop_primary_old)), it.startIndex, it.startIndex+it.word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if ( it.styleFormat == Styles.BOLD) {
                    spannableStringBuilder.setSpan(StyleMakeSpan(Styles.BOLD,""),it.startIndex,it.endIndex,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if ( it.styleFormat == Styles.ITALIC) {
                    spannableStringBuilder.setSpan(StyleMakeSpan(Styles.ITALIC,""),it.startIndex,it.endIndex,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if ( it.styleFormat == Styles.UNDER_LINE) {
                    spannableStringBuilder.setSpan(StyleMakeSpan(Styles.UNDER_LINE,""),it.startIndex,it.endIndex,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if ( it.styleFormat == Styles.STRIKE) {
                    spannableStringBuilder.setSpan(StyleMakeSpan(Styles.STRIKE,""),it.startIndex,it.endIndex,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                spannableStringBuilder = SpannableStringBuilder(spannableStringBuilder)
                setText(spannableStringBuilder,BufferType.SPANNABLE)
            }
//        }
//        catch (e:Exception){
//
//        }
    }

}

