package com.example.markdownapp.richtextlib

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View

/**
 * Mention & Link View
 * */
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


/**
 * Bold,Italic,Strike & Underline
 * */
class StyleMakeSpan (
    private val styles: Styles,
    private val selectedText : String
) : StyleSpan(styles.typeface) {
   
   
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        
        ds.typeface = Typeface.create(ds.typeface, styles.typeface)
        when(styles){
            Styles.BOLD -> Unit
            Styles.ITALIC -> Unit
            Styles.MENTION -> Unit
            Styles.LINK -> Unit
            Styles.STRIKE -> {
                ds.isStrikeThruText = true
            }
            Styles.UNDER_LINE->{
                ds.isUnderlineText = true
            }
            Styles.PLAIN -> Unit
        }
    }
    
    
    fun getChangedName(): String = selectedText
    fun getStyleNameName(): Styles = styles
    fun getTypeFace(): Int = styles.typeface
}
