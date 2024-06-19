package com.hwl.chatbotapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

        // Initialize the adapter with an empty list
        adapter = MessageAdapter(listOf(), mainViewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Collecting the StateFlow from the ViewModel
        mainViewModel.uiState.observe(this) { uiState ->
            Log.d("MainActivity", "UI State is being collected")
            // 更新适配器的数据
            adapter.setMessages(uiState.messages)
        }

        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessage.text.toString()
            if (userMessage.isNotEmpty()) {
                Log.d("TAG", "onCreate: $userMessage")
                mainViewModel.sendMessage(userMessage)
                binding.etMessage.setText("") // Clear the text field
            }
        }
    }
}
