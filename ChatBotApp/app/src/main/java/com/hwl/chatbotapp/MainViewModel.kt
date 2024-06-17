package com.hwl.chatbotapp

import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import com.hwl.chatbotapp.app.App
import java.util.*

class MainViewModel() : ViewModel() {
    private val onInitListener = object : OnInitListener {
        override fun onInit(status: Int) {
            ttsReady = status != TextToSpeech.ERROR
        }
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            isSpeak = true
        }

        override fun onDone(utteranceId: String?) {
            isSpeak = false
        }

        override fun onError(utteranceId: String?) {
            isSpeak = false
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            isSpeak = !interrupted
            super.onStop(utteranceId, interrupted)
        }

    }

    val tts = TextToSpeech(App.INSTANCE, onInitListener, "com.hwl.chatbotapp")


    var ttsReadyState: Boolean = false
    var ttsReady: Boolean
        get() = ttsReadyState
        set(value) {
            ttsReadyState = value
        }

    var speakState: Boolean = false
    private var isSpeak: Boolean
        get() = speakState
        set(value) {
            speakState = value
        }

    val appVer: String = try {
        App.INSTANCE.packageManager.getPackageInfo(App.INSTANCE.packageName, 0).versionName
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    init {
        tts.setOnUtteranceProgressListener(progressListener)
    }

    fun sayText(text: String) {
        val result = tts.setLanguage(Locale.CHINESE)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        } else {
            val speechResult = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, Random().nextInt().toString())
        }
    }

    fun stop() {
        tts.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }
}