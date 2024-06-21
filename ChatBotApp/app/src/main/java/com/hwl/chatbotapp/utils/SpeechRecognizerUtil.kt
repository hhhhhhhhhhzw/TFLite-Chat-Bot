package com.hwl.chatbotapp.utils

import android.util.Log
import com.alibaba.fastjson.JSONPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SpeechRecognizerUtil {
    private const val URL = "https://nls-gateway-cn-shanghai.aliyuncs.com/stream/v1/asr"
    private const val APP_KEY = "appkey"
    private const val APP_SECRET = "token"
    suspend fun process(
        fileName: String,
        format: String = "pcm",
        sampleRate: Int = 16000,
        enablePunctuationPrediction: Boolean = true,
        enableInverseTextNormalization: Boolean = true,
        enableVoiceDetection: Boolean = false
    ): String? {
        return withContext(Dispatchers.IO) {
            var request = "$URL?appkey=$APP_KEY&format=$format&sample_rate=$sampleRate"
            if (enablePunctuationPrediction) {
                request += "&enable_punctuation_prediction=true"
            }
            if (enableInverseTextNormalization) {
                request += "&enable_inverse_text_normalization=true"
            }
            if (enableVoiceDetection) {
                request += "&enable_voice_detection=true"
            }

            val headers = HashMap<String, String>().apply {
                put("X-NLS-Token", APP_SECRET)
                put("Content-Type", "application/octet-stream")
            }

            HttpUtil.sendPostFile(request, headers, fileName)?.let { response ->
                Log.d("http", "process: $response")
                val result = JSONPath.read(response, "result").toString()
                result
            }
        }
    }
}