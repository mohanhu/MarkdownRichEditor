package com.example.markdownapp

import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.markdownapp.databinding.FragmentEditorBinding
import com.example.markdownappapp.richlib.NeedPatternList
import com.example.markdownappapp.richlib.PATTERN_TYPE
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditorFragment : Fragment() ,MarkDownCallBack{

    private val binding : FragmentEditorBinding by lazy { FragmentEditorBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { super.onViewCreated(view, savedInstanceState)

        // Bold button click listener
        binding.boldButton.setOnClickListener {
            wrapTextWithMarkdown(binding.overlayEditText, "**")
        }

        // Strike button click listener
        binding.strikeButton.setOnClickListener {
            wrapTextWithMarkdown(binding.overlayEditText, "~~")
        }

        // Italic button click listener
        binding.italicButton.setOnClickListener {
            wrapTextWithMarkdown(binding.overlayEditText, "_")
        }

        binding.bulletButton.setOnClickListener {
            addBulletList(binding.overlayEditText)
        }

        binding.mentionButton.setOnClickListener {
            addMention(binding.overlayEditText,"<@3276534>")
        }

        binding.linkButton.setOnClickListener {
            addLinkMarkdown(binding.overlayEditText,"https://medium.com/@shaikvazid333")
        }
        
        binding.overlayEditText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Unit
            }

            override fun afterTextChanged(s: Editable?) {
                binding.tvMarkDown.visibility = View.INVISIBLE
                binding.selector.isSelected = false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        val markwon = Markwon.builder(requireContext())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()

        val editor = MarkwonEditor.builder(markwon).build()
        binding.overlayEditText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))

        binding.selector.setOnClickListener {
            binding.selector.isSelected = !binding.selector.isSelected
            binding.tvMarkDown.isVisible = binding.selector.isSelected
            if (binding.selector.isSelected){
                binding.tvMarkDown.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    binding.tvMarkDown.text = binding.overlayEditText.text.toString().replace("\n", "  \n")
                    val needPatternList = listOf<NeedPatternList>(
                        NeedPatternList(neededType = PATTERN_TYPE.MENTION, R.color.golden_yellow),
                        NeedPatternList(neededType = PATTERN_TYPE.URL_PATTERN, R.color.button_blue_color),
                    )
                    binding.tvMarkDown.getInstance(this@EditorFragment)
                    binding.tvMarkDown.startPatternRecognition(markDown = true,needPatternList = needPatternList)
                }
            }
        }
    }


    private fun addBulletList(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        val selectedText = editText.text.substring(start, end)

        // Split the selected text into lines
        val lines = selectedText.split("\n")

        // Add a bullet point to the beginning of each line
        val bulletListText = lines.joinToString("\n") { "- $it" }

        // Replace the selected text with the bullet list
        editText.text.replace(start, end, bulletListText)

        // Adjust the cursor after replacing the text
        editText.setSelection(start + bulletListText.length)
    }

    private fun wrapTextWithMarkdown(editText: EditText, markdown: String) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        val selectedText = editText.text.substring(start, end)

        // Wrap selected text with the markdown symbol
        val newText = " $markdown$selectedText$markdown "
        editText.text.replace(start, end, newText)

        // Move the cursor to after the inserted markdown
        editText.setSelection(start + markdown.length + selectedText.length+1)
    }

    private fun addMention(editText: EditText, mention: String) {
        val start = editText.selectionStart
        val selectedText = editText.text.substring(0,start) + " $mention "+editText.text.substring(start,editText.text.length)
        editText.setText(selectedText)
        editText.setSelection(start+mention.length+2)
    }

    private fun addLinkMarkdown(editText: EditText, url: String) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        val selectedText = "Click here:"

        // Wrap selected text with the markdown link format
        val newText = " [Click here:]($url) "
        editText.text.replace(start, end, newText)

        // Move the cursor to after the inserted markdown
        editText.setSelection(start + newText.length)
    }

    override fun mentionOnClick(mentionId: String) {
        Toast.makeText(requireContext(), "$mentionId", Toast.LENGTH_SHORT).show()
    }

    override fun urlOnClick(url: String) {
        Toast.makeText(requireContext(), "$url", Toast.LENGTH_SHORT).show()
    }
}