package com.example.markdownapp.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.markdownapp.R
import com.example.markdownapp.databinding.FragmentEditorBinding
import com.example.markdownapp.spanrichlib.BlockExport.blockJsonExportToEdit
import com.example.markdownapp.spanrichlib.BlockExport.blockJsonToExportDataTextView
import com.example.markdownapp.spanrichlib.BlockKit.blockKitListGenerate
import com.example.markdownapp.spanrichlib.BlockKitData
import com.example.markdownapp.spanrichlib.BlockKitManage
import com.example.markdownapp.spanrichlib.RichSpanCallBack
import com.example.markdownapp.spanrichlib.RichSpanDownStyle.listenCurrentStyleFormat
import com.example.markdownapp.spanrichlib.RichSpanDownStyle.makeStyleFormat
import com.example.markdownapp.spanrichlib.RichSpanDownStyle.onTypeStateChange
import com.example.markdownapp.spanrichlib.RichSpanDownStyle.toggleStyle
import com.example.markdownapp.spanrichlib.NeedPatternList
import com.example.markdownapp.spanrichlib.PATTERN_TYPE
import com.example.markdownapp.spanrichlib.StyleActionBindClick.addBulletList
import com.example.markdownapp.spanrichlib.StyleActionBindClick.addLinkMarkdown
import com.example.markdownapp.spanrichlib.StyleActionBindClick.addMention
import com.example.markdownapp.spanrichlib.StyleActionBindClick.currentLineStartsWithDash
import com.example.markdownapp.spanrichlib.StyleActionBindClick.editAddLink
import com.example.markdownapp.spanrichlib.StyleActionBindClick.editAddMention
import com.example.markdownapp.spanrichlib.Styles
import com.example.markdownapp.utils.redirectToChrome
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EditorFragment : Fragment() , RichSpanCallBack {

    companion object{
        const val timber = "Typeface.BOLD::class.java"
    }

    private val binding : FragmentEditorBinding by lazy { FragmentEditorBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<RIchTextViewModel>()

    private var LAST_RICH_EDITOR_CURSOR_POSITION = 1

    private var mentionDataClass = mutableListOf<BlockKitManage>()

    private var apiEditData = JsonObject()

    private var currentTypeStyle = Styles.PLAIN

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { super.onViewCreated(view, savedInstanceState)

        binding.bindList()

        onClickListeners()

        bindMarkMarkdown()

        textWatchLister()

        senderAndReceiver()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.map { it.currentIndex }.distinctUntilChanged().collectLatest {}
            }
        }
    }

    private fun senderAndReceiver() {
        binding.sendBtn.setOnClickListener {

            lifecycleScope.launch(Dispatchers.Main) {
                val gson = GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().create()

                val blockCode = binding.overlayEditText.blockKitListGenerate()

                val jsonObject = JsonObject()
                jsonObject.addProperty("text",blockCode.text)
                JsonArray().apply {
                    blockCode.block.forEach{ data ->
                        add((JsonObject().also {
                            it.addProperty("key",data.key)
                            it.addProperty("word",data.word)
                            it.addProperty("pattern",data.pattern)
                            it.addProperty("styleFormat",data.styleFormat.name)
                        }))
                    }
                }.also { array->
                    jsonObject.add("block",array)
                }

                println("EditText.blockKitListGenerate() >>> ${blockCode.block}")
                println("EditText.blockKitListGenerate() >>> $jsonObject")

                mentionDataClass = gson.fromJson(jsonObject,BlockKitData::class.java).block.toMutableList()
                println("$timber >>> preview in mention data class >>> $mentionDataClass")
                println("$timber >>> preview json file file mention >>> $jsonObject")
                apiEditData = jsonObject

                buildSentenceFromMentions(blockCode.block)

            }
//            binding.overlayEditText.removeAllSpans()
        }

        binding.tvMarkDown.setOnClickListener {
            currentTypeStyle  = Styles.PLAIN
            binding.overlayEditText.blockJsonExportToEdit(apiEditData.toString())
        }
    }

    private fun textWatchLister() {
        binding.overlayEditText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                lifecycleScope.launch(Dispatchers.Main){
                    val blockJsonFile = binding.overlayEditText.blockKitListGenerate()
                    println("binding.overlayEditText.blockKitListGenerate >>> 123 >>>$blockJsonFile")
                }

                val cursor = binding.overlayEditText.selectionStart?.takeIf { it>=0 }?:0
                if(LAST_RICH_EDITOR_CURSOR_POSITION < cursor) {
                    println("binding.overlayEditText.addTextChangedListener >>> satisfied ${binding.overlayEditText.selectionStart}")
//                    binding.overlayEditText.handleInBetweenStylesChar(start,count,currentTypeStyle)
                }
//                println("EditText.handleMarkDownWatcher start >> gothrough char >>>Last>$LAST_RICH_EDITOR_CURSOR_POSITION start>$start >>before>$before >>count>$count")
//                if (before > 0 && count == 0 && binding.overlayEditText.selectionStart>0) { }
            }

            override fun afterTextChanged(s: Editable?) {
                val cursor = binding.overlayEditText.selectionStart?.takeIf { it>=0 }?:0
                if (LAST_RICH_EDITOR_CURSOR_POSITION < cursor) {
                    println("EditText.onTypeStateChange >>>> $cursor >>${s?.length}")
                    binding.overlayEditText.apply {
                        onTypeStateChange(cursor, currentTypeStyle)
                    }
                }
                if (binding.overlayEditText.currentLineStartsWithDash() && LAST_RICH_EDITOR_CURSOR_POSITION < cursor) {
                    binding.overlayEditText.text?.insert(cursor, "• ")
                }
                LAST_RICH_EDITOR_CURSOR_POSITION = cursor
            }
        })
    }

    private fun bindMarkMarkdown() {
        val markwon = Markwon.builder(requireContext())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(CorePlugin.create())
            .build()

        val editor = MarkwonEditor.builder(markwon).build()

        binding.overlayEditText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))
        binding.overlayEditText.setTextIsSelectable(true)
        binding.overlayEditText.setSpannableFactory(Spannable.Factory.getInstance())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onClickListeners() {
        binding.boldButton.setOnClickListener {
            currentTypeStyle = if (currentTypeStyle == Styles.BOLD){
                binding.overlayEditText.toggleStyle(Styles.PLAIN)
                Styles.PLAIN
            } else {
                binding.overlayEditText.toggleStyle(Styles.BOLD)
                Styles.BOLD
            }
        }

        binding.italicButton.setOnClickListener {
            currentTypeStyle = if (currentTypeStyle == Styles.ITALIC){
                binding.overlayEditText.toggleStyle(Styles.PLAIN)
                Styles.PLAIN
            } else {
                binding.overlayEditText.toggleStyle(Styles.ITALIC)
                Styles.ITALIC
            }
        }

        binding.mentionButton.setOnClickListener {
            binding.rvOptions.visibility = View.VISIBLE
        }

        binding.linkButton.setOnClickListener {
            binding.overlayEditText.addLinkMarkdown("https://pepul.workfast.ai/channel")
        }

        binding.bulletButton.setOnClickListener {
            binding.overlayEditText.addBulletList()
        }

        binding.overlayEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v.post {
                    binding.overlayEditText.listenCurrentStyleFormat{ styles: Styles ->
                        currentTypeStyle = styles
                        LAST_RICH_EDITOR_CURSOR_POSITION = binding.overlayEditText.selectionStart/**/
                    }
                    println("binding.overlayEditText.setOnTouchListener ACTION_UP >>1>$currentTypeStyle")
                }
            }
            false
        }
    }

    private fun FragmentEditorBinding.bindList() {
        val mentionListAdapter = MentionListAdapter(
            onClickId = {
                binding.overlayEditText.addMention(mentionId = it.userId, mention = it.userName){_,_-> }
                binding.rvOptions.visibility = View.GONE
            }
        )
        binding.rvOptions.layoutManager =LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.rvOptions.adapter = mentionListAdapter
        mentionListAdapter.submitList(MentionClass.List.mentionDataClass)
    }

    private suspend fun buildSentenceFromMentions(mentionList: List<BlockKitManage>) {
        val blockList = binding.tvMarkDown.blockJsonToExportDataTextView(apiEditData.toString())

        val needPatternList = listOf<NeedPatternList>(
            NeedPatternList(neededType = PATTERN_TYPE.URL_PATTERN, R.color.button_blue_color),
        )
        binding.tvMarkDown.getInstance(this@EditorFragment)
        binding.tvMarkDown.startPatternRecognition(
            markDown = false ,
            needPatternList = needPatternList,
            mentionList = blockList
        )
    }

    override fun mentionOnClick(mentionId: String) {
        println("profileFragment mentionId >>>$mentionId")
        findNavController().navigate(R.id.profileFragment, bundleOf("profileFragment" to mentionId))
    }

    override fun urlOnClick(url: String) {
        redirectToChrome(requireActivity(), urls = url)
    }
}
