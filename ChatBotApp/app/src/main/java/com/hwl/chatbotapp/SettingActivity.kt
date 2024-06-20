package com.hwl.chatbotapp

//noinspection SuspiciousImport
import android.R
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.hwl.chatbotapp.app.App
import com.hwl.chatbotapp.databinding.ActivitySettingBinding
import com.hwl.chatbotapp.llm.InferenceModel
import com.hwl.chatbotapp.models.TtsType
import com.hwl.chatbotapp.tts.TtsManager
import com.hwl.chatbotapp.tts.TtsManager.speed
import java.io.File

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var llmModels : List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 检查当前主题偏好并应用
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.darkSwitch.isChecked = false
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.darkSwitch.isChecked = true
            } // Night mode is active, we're using dark theme
        }
        // 通过Switch控制深浅模式
        binding.darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            setNightMode(isChecked)
        }
        // 返回键
        binding.returnButton.setOnClickListener {
            finish()
        }
        // 展示哪个速度被选中
        if (loadSpeed()==1.2f){
            binding.ttsSpeed1.isChecked = true
            binding.ttsSpeed2.isChecked = false
            binding.ttsSpeed3.isChecked = false
        } else if (loadSpeed()==1.0f){
            binding.ttsSpeed1.isChecked = false
            binding.ttsSpeed2.isChecked = true
            binding.ttsSpeed3.isChecked = false
        } else{
            binding.ttsSpeed1.isChecked = false
            binding.ttsSpeed2.isChecked = false
            binding.ttsSpeed3.isChecked = true
        }
        // 展示哪个tts模型被选择
        if (loadTtsModel()==1){
            binding.ttsModel1.isChecked = true
            binding.ttsModel2.isChecked = false
        }else{
            binding.ttsModel1.isChecked = false
            binding.ttsModel2.isChecked = true
        }
        //  选择模型
        if (binding.ttsModel2.isChecked){
            binding.ttsSpeed1.isEnabled = false
            binding.ttsSpeed2.isChecked = true
            binding.ttsSpeed3.isEnabled = false
        } else{
            binding.ttsSpeed1.isEnabled = true
            binding.ttsSpeed3.isEnabled = true
        }
        binding.ttsModel1.setOnClickListener {
            binding.ttsSpeed1.isEnabled = true
            binding.ttsSpeed3.isEnabled = true
            TtsManager.type = TtsType.FASTSPEECH2
            setTtsModel(1)
        }
        binding.ttsModel2.setOnClickListener {
            binding.ttsSpeed1.isEnabled = false
            binding.ttsSpeed2.isChecked = true
            binding.ttsSpeed3.isEnabled = false
            TtsManager.type = TtsType.TACOTRON2
            TtsManager.speed = 1.0f
            setTtsSpeed(1.0f)
            setTtsModel(2)
        }
        binding.ttsSpeed1.setOnClickListener {
            TtsManager.speed = 1.2f
            setTtsSpeed(1.2f)
        }
        binding.ttsSpeed2.setOnClickListener {
            TtsManager.speed = 1.0f
            setTtsSpeed(1.0f)
        }
        binding.ttsSpeed3.setOnClickListener {
            TtsManager.speed = 0.8f
            setTtsSpeed(0.8f)
        }
//        val path = "/data/local/tmp/llm/"
//        llmModels = listBinFiles(path)
//        // 创建一个ArrayAdapter用来展示llm模型列表
//        val llmAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, llmModels)
//        llmAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
//        // 将llmAdapter设置给llmSpinner
//        binding.llmSpinner.adapter = llmAdapter
//        // 设置llmSpinner的选择事件监听器
//        binding.llmSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                // 修改模型
//                val selectedItem = parent.getItemAtPosition(position).toString()
//                InferenceModel.getInstance(App.INSTANCE).setModel(selectedItem)
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // 未选择任何项
//            }
//        }
    }
    // 保存偏好到SharedPreferences
    fun setNightMode(isNightMode: Boolean) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean("NightMode", isNightMode)
            apply()
        }
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    // 保存ttsSpeed偏好到SharedPreferences
    fun setTtsSpeed(speed: Float) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        with(prefs.edit()) {
            putFloat("speed", speed)
            apply()
        }
    }
    // 保存ttsModel偏好到SharedPreferences
    fun setTtsModel(ttsModel: Int) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        with(prefs.edit()) {
            putInt("ttsModel", ttsModel)
            apply()
        }
    }
    // 从SharedPreferences获取ttsSpeed
    fun loadSpeed(): Float {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        return prefs.getFloat("speed", 1.0f) // 默认为1.0f
    }
    // 从SharedPreferences获取ttsModel
    fun loadTtsModel(): Int {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        return prefs.getInt("ttsModel", 1) // 默认为1
    }
    // 查找大语言模型文件
    fun listBinFiles(directoryPath: String): List<String> {
        val directory = File(directoryPath)
        // 确保传入的路径是一个目录
        if (!directory.isDirectory) {
            println("Provided path is not a directory.")
            return emptyList()
        }
        // 使用 filter 和 map 函数来筛选和转换数据
        return directory.listFiles() // 获取目录中的所有文件和文件夹
            ?.filter { it.isFile && it.name.endsWith(".bin") } // 筛选出是文件且以 .bin 结尾的
            ?.map { it.nameWithoutExtension } // 获取不含扩展名的文件名
            ?: emptyList() // 如果 listFiles 返回 null，使用空列表
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("NightMode", binding.darkSwitch.isChecked)
    }


}