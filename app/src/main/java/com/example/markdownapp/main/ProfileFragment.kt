package com.example.markdownapp.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.markdownapp.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private val binding : FragmentProfileBinding by lazy { FragmentProfileBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bundle = arguments

        if (bundle?.containsKey("profileFragment") == true) {
            val id = bundle?.getString("profileFragment") ?: ""

            val data =
                MentionClass.List.mentionDataClass.firstOrNull { it.userId == id } ?: MentionClass()

            Glide.with(requireContext())
                .load("https://www.lawhousekolkata.com/wp-content/uploads/Blog/Abdul-Kalam/abdul-kalam.jpg")
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivMarkDown)

            binding.tvMarkDown.text = data.userName

        }
    }

}