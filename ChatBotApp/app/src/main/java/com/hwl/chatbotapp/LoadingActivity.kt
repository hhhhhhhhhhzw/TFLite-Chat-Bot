package com.hwl.chatbotapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hwl.chatbotapp.app.App
import com.hwl.chatbotapp.databinding.ActivityLoadingBinding
import com.hwl.chatbotapp.llm.InferenceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoadingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadModel()

    }
    private fun loadModel(){
        lifecycleScope.launch(Dispatchers.IO){
            try {
                InferenceModel.getInstance(App.INSTANCE)
                // 跳转到主页面
                startActivity(Intent(this@LoadingActivity, MainActivity::class.java))
                finish()  // 关闭当前活动，避免用户返回到加载屏
            } catch (e: Exception) {
                Log.e("LoadingActivity", "onCreate: ${e.message}", )
            }
        }
    }
}