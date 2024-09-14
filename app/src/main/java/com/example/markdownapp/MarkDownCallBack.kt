package com.example.markdownapp

interface MarkDownCallBack {

    fun mentionOnClick(mentionId: String)
    fun urlOnClick(url: String)

}