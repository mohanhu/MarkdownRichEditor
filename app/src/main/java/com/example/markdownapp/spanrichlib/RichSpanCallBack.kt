package com.example.markdownapp.spanrichlib

interface RichSpanCallBack {

    fun mentionOnClick(mentionId: String)

    fun urlOnClick(url: String)

}