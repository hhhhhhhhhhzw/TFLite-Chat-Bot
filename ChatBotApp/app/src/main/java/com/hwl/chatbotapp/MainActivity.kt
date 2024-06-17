package com.hwl.chatbotapp

import MessageAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hwl.chatbotapp.databinding.ActivityMainBinding
import com.hwl.chatbotapp.tts.TtsManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContentView(binding.root)
        // 初始化TTS引擎
        TtsManager.initModels(this)

        // 设置适配器和布局管理器
        adapter = MessageAdapter(messages,mainViewModel)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 处理发送按钮的点击事件
        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessage.text.toString()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, true)
                binding.etMessage.setText("")
                simulateBotResponse(userMessage)
            }
        }
    }
    // 添加消息到列表并通知适配器更新
    private fun addMessage(content: String, isUser: Boolean) {
        messages.add(Message(content, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1) // 自动滚动到最新消息
    }

    // 模拟机器人回复
    private fun simulateBotResponse(userMessage: String) {
        // 模拟回复逻辑，这里简单回复用户的消息
        val botMessage = "$userMessage"
        addMessage(botMessage, false)
    }
}

