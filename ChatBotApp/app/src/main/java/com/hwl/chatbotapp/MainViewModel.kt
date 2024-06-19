package com.hwl.chatbotapp

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hwl.chatbotapp.app.App
import com.hwl.chatbotapp.llm.InferenceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel : ViewModel() {
    private val inferenceModel = InferenceModel.getInstance(App.INSTANCE)
    private val tts = TextToSpeech(App.INSTANCE, { status ->
        ttsReady = status != TextToSpeech.ERROR
    }, "com.hwl.chatbotapp")
    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) { isSpeak = true }
        override fun onDone(utteranceId: String?) { isSpeak = false }
        override fun onError(utteranceId: String?) { isSpeak = false }
        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            isSpeak = !interrupted
            super.onStop(utteranceId, interrupted)
        }
    }

    private val _uiState: MutableStateFlow<GemmaUiState> = MutableStateFlow(GemmaUiState())
    val uiState: LiveData<GemmaUiState> = _uiState.asLiveData()
    private var messages = mutableListOf<ChatMessage>()

    fun getMessages(): MutableList<ChatMessage> {
        return messages
    }
    fun setMessages(messages: MutableList<ChatMessage>) {
        this.messages = messages
    }
    fun addMessage(message: ChatMessage) {
        messages.add(message)
    }
    fun updateMessage(string: String) {
        messages[messages.size-1].message = string
    }

    private val _textInputEnabled: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val isTextInputEnabled: StateFlow<Boolean> =
        _textInputEnabled.asStateFlow()

    private var ttsReadyState: Boolean = false
    var ttsReady: Boolean
        get() = ttsReadyState
        set(value) {
            ttsReadyState = value
        }

    private var speakState: Boolean = false
    private var isSpeak: Boolean
        get() = speakState
        set(value) {
            speakState = value
        }

    init {
        tts.setOnUtteranceProgressListener(progressListener)
    }
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> = _response


    fun sendMessage(userMessage: String) {
        Log.d("MainViewModel", "Adding message: $userMessage")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.addMessage(userMessage, USER_PREFIX)
            var currentMessageId: String? = _uiState.value.createLoadingMessage()
            setInputEnabled(false)
            try {
                val fullPrompt = _uiState.value.fullPrompt
                var responseAsync = inferenceModel.generateResponseAsync(fullPrompt)
                _response.postValue(responseAsync)
                setInputEnabled(true)
//                inferenceModel.partialResults
//                    .collectIndexed { index, (partialResult, done) ->
//                        currentMessageId?.let {
//                            if (index == 0) {
//                                _uiState.value.appendFirstMessage(it, partialResult)
//                            } else {
//                                _uiState.value.appendMessage(it, partialResult, done)
//                            }
//                            if (done) {
//                                currentMessageId = null
//                                // Re-enable text input
//                                setInputEnabled(true)
//                            }
//                        }
//                    }
            } catch (e: Exception) {
                _uiState.value.addMessage(e.localizedMessage ?: "Unknown Error", MODEL_PREFIX)
                setInputEnabled(true)
            }
        }
    }

    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
    }

    fun sayText(text: String) {
        if (ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        }
    }

    fun stop() {
        tts.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }

    companion object {
        fun getFactory() = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel() as T
            }
        }
    }
}
