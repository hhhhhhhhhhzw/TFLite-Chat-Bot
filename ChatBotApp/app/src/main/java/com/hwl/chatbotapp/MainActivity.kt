package com.hwl.chatbotapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hwl.chatbotapp.databinding.ActivityMainBinding
import com.hwl.chatbotapp.tts.TtsManager
import com.hwl.chatbotapp.utils.AudioRecorderUtil
import com.hwl.chatbotapp.utils.SpeechRecognizerUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var mainViewModel: MainViewModel
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val isNightModeEnabled = loadNightMode()
        AppCompatDelegate.setDefaultNightMode(
            if (isNightModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        TtsManager.initModels(this)
        adapter = MessageAdapter(mainViewModel.getMessages(), mainViewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        if (mainViewModel.getMessages().isEmpty()){
            mainViewModel.addMessage(ChatMessage(message =  "你好！我是你的机器人助手。请问您今天有什么问题或需要帮助的吗？", author = MODEL_PREFIX))
        }
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
        binding.settingButton.setOnClickListener {
            // 跳转到设置页面
            startActivity(Intent(this@MainActivity, SettingActivity::class.java))
        }
        binding.btnBeginRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf( android.Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            } else {
                startRecording()
            }
            binding.btnBeginRecord.isVisible = false
            binding.btnStopRecord.isVisible = true
        }
        binding.btnStopRecord.setOnClickListener {
            stopRecording()
            processAudio()
            binding.btnBeginRecord.isVisible = true
            binding.btnStopRecord.isVisible = false
        }
    }
    // 从SharedPreferences获取是否使用深色模式
    fun loadNightMode(): Boolean {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        return prefs.getBoolean("NightMode", false) // 默认为 false
    }

    private fun startRecording() {
        AudioRecorderUtil.startRecording()
//        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        AudioRecorderUtil.stopRecording()
//        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
    }

    private fun processAudio() {
        val audioFilePath = AudioRecorderUtil.getAudioFilePath()
        Log.d("TAG", "processAudio: $audioFilePath")
        CoroutineScope(Dispatchers.Main).launch {
            val result = SpeechRecognizerUtil.process(audioFilePath)
            // 更新输入框
            binding.etMessage.text = result.toEditable()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
    // 扩展函数，用于将String?转换为Editable
    fun String?.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}
