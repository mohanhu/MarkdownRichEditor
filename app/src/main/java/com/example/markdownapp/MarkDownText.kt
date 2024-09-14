package com.example.markdownapp

import android.content.Context
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
import com.example.markdownappapp.richlib.NeedPatternList
import com.example.markdownappapp.richlib.PATTERN_TYPE
import com.example.markdownappapp.richlib.PatternString
import com.example.markdownappapp.richlib.needPatternList
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

 /*
 * Text color conversion based on PATTERN_TYPE
 * */

class MarkDownText(context: Context,attributeSet: AttributeSet) : AppCompatTextView(context,attributeSet) {

    private lateinit var markDownCallBack: MarkDownCallBack

    fun getInstance(mark:MarkDownCallBack) {
        markDownCallBack = mark
    }

    private var spannableStringBuilder : SpannableStringBuilder = SpannableStringBuilder("")

    private val markwon = Markwon.builder(context)
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .build()

    override fun setText(text: CharSequence?, type: BufferType?) { super.setText(text, type) }
    suspend fun startPatternRecognition(markDown: Boolean, needPatternList: List<NeedPatternList>) {
        spannableStringBuilder = if (markDown){
            SpannableStringBuilder(markwon.toMarkdown(text.toString()))
        }
        else{
            SpannableStringBuilder(text.toString())
        }
        val findString = needPatternList(spannableStringBuilder.toString(),needPatternList)
        if(findString.isNotEmpty())spanStringUpdate(findString)
        else {
            if (markDown) setText(markwon.toMarkdown(text.toString()))
        }
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = ContextCompat.getColor(context,R.color.transparent)
    }

    private suspend fun spanStringUpdate(findString: List<PatternString>) {
        try {
            findString.toList().asReversed().forEachIndexed { _, patternString ->
                var changeName = ""
                withContext(Dispatchers.IO){
                    changeName = " @Mohan "
                }
                val start = patternString.start
                val end = patternString.end

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        when(patternString.patternType){
                            PATTERN_TYPE.MENTION -> {
                                val id = "<@(\\d+)>".toRegex()
                                val idValue = id.find(patternString.patternValue)?.groups?.get(1)?.value
                                println("spanStringUpdate >>> $idValue")
                                markDownCallBack.mentionOnClick(patternString.patternValue)
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
                    if (patternString.patternType==PATTERN_TYPE.MENTION){
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
        }
        catch (_:Exception){}
    }
}