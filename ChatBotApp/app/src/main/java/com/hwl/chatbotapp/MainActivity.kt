package com.hwl.chatbotapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hwl.chatbotapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var mainViewModel: MainViewModel

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

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
