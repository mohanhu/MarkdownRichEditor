package com.example.markdownapp.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.markdownapp.databinding.MentionItemLayoutBinding

class MentionListAdapter (
    private val onClickId:(MentionClass)->Unit
):ListAdapter<MentionClass, MentionListAdapter.ViewHolderOne>(DifferMention) {

    inner class ViewHolderOne(private val binding: MentionItemLayoutBinding):RecyclerView.ViewHolder(binding.root){
        fun bindItem(dataClass: MentionClass){
            Glide.with(itemView.context).load(
                "https://www.lawhousekolkata.com/wp-content/uploads/Blog/Abdul-Kalam/abdul-kalam.jpg"
            ).diskCacheStrategy(DiskCacheStrategy.ALL).apply(RequestOptions.circleCropTransform()).into(binding.ivUserImage)
            binding.tvUserName.text = dataClass.userName
            itemView.setOnClickListener {
                onClickId.invoke(dataClass)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderOne {
        return ViewHolderOne(binding = MentionItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolderOne, position: Int) {
        holder.bindItem(getItem(position))
    }

}

object DifferMention : DiffUtil.ItemCallback<MentionClass>() {
    override fun areItemsTheSame(oldItem: MentionClass, newItem: MentionClass): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: MentionClass, newItem: MentionClass): Boolean {
        return oldItem == newItem
    }

}


data class MentionClass (
    val userId:String = "0",
    val userName: String = ""
){
    object List {
        val mentionDataClass = listOf(
            MentionClass(userId = "0", userName = "Person1"),
            MentionClass(userId = "1",userName = "Person2"),
            MentionClass(userId = "2",userName = "Person3"),
            MentionClass(userId = "3",userName = "Person4"),
            MentionClass(userId = "4",userName = "Person5")
        )
    }
}