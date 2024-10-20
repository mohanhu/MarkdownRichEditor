package com.example.markdownapp.richtextlib

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View

class MentionClickableSpan(
    private val mentionName: String,
    private val mentionId: String,
    private val onClick: (mentionName: String, mentionId: String) -> Unit
) : ClickableSpan() {

    private var styleOfClickable = Styles.MENTION

    override fun onClick(widget: View) {
        onClick(mentionName, mentionId)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
        if (styleOfClickable == Styles.LINK){
            ds.isUnderlineText = true
        }
    }

    fun setCurrentStyle(styles: Styles){
        styleOfClickable = styles
    }

    fun getCurrentStyle() = styleOfClickable

    fun getMentionId(): String = mentionId
    fun getMentionName(): String = mentionName
}

class StyleMakeSpan (
    private val typeface: Int,
    private val selectedText : String
) : StyleSpan(typeface) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.typeface = Typeface.create(ds.typeface, typeface)
    }
    fun getChangedName(): String = selectedText
}
