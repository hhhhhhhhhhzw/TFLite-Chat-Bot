package com.hwl.chatbotapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hwl.chatbotapp.databinding.ActivityMainBinding
import com.hwl.chatbotapp.tts.TtsManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var mainViewModel: MainViewModel

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 显示进度条
        binding.progressBar.visibility = View.VISIBLE
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        TtsManager.initModels(this)
        adapter = MessageAdapter(mainViewModel.getMessages(), mainViewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessage.text.toString()
            if (userMessage.isNotEmpty()) {
                Log.d("TAG", "onCreate: $userMessage")
                mainViewModel.addMessage(ChatMessage(message =  userMessage, author = USER_PREFIX))
                mainViewModel.addMessage(ChatMessage(message =  "思考中……", author = MODEL_PREFIX))
                adapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                mainViewModel.sendMessage(userMessage)
                binding.etMessage.setText("") // Clear the text field
            }
        }
        mainViewModel.response.observe(this, Observer { response ->
            // 处理响应
            mainViewModel.updateMessage(response)
            adapter.notifyDataSetChanged()
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        })
    }
}
