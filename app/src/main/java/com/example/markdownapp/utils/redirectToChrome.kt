package com.example.markdownapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun redirectToChrome(context: Activity, urls: String) {

        val urlRegex = "(?:(?:https?|ftp)://)?[\\w/d\\-?=%.]+\\.[\\w/d\\-?=%.]+".toRegex()

        val url = urlRegex.find(urls)?.value?:""

        println("callBackTextString >>>${url} >>$urls")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val browse = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                // Check if it's a YouTube URL
                if (url.startsWith("https://www.youtube.com/") || url.startsWith("http://www.youtube.com/")) {
                    browse.setPackage("com.google.android.youtube")
                }

                if (browse.resolveActivity(context.packageManager) != null) {
                    context.startActivity(browse)
                } else {
                    // Handle the case where there is no app to handle the Intent
                    context.startActivity(browse)
                }
            }
        } catch (e: Exception) {
        }
    }
