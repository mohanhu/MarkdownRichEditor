package com.example.markdownapp.richtextlib

interface MarkDownCallBack {

    fun mentionOnClick(mentionId: String)

    fun urlOnClick(url: String)

}