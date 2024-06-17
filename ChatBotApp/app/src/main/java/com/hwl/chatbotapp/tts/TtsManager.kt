package com.hwl.chatbotapp.tts

import android.content.Context
import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.benjaminwan.moshi.utils.moshiAny
import com.hwl.chatbotapp.app.App
import com.hwl.chatbotapp.common.FASTSPEECH2_NAME
import com.hwl.chatbotapp.common.MELGAN_NAME
import com.hwl.chatbotapp.common.TACOTRON2_NAME
import com.hwl.chatbotapp.common.targetDir
import com.hwl.chatbotapp.models.TtsType
import com.hwl.chatbotapp.utils.ZhProcessor
import com.hwl.chatbotapp.utils.copyAssetFileToDir
import kotlinx.coroutines.*
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

object TtsManager {
    private const val SP_APP = "sp_tts_app"
    private const val SP_TTS_TYPE = "sp_tts_type"
    private const val SP_TTS_SPEED = "sp_tts_speed"

    private const val TTS_SAMPLE_RATE = 24000
    private const val TTS_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    private var fastSpeech: FastSpeech2? = null
    private var tacotron: Tacotron2? = null
    private var melGan: MBMelGan? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var inputTextJob: Job? = null//在Main线程调用，避免线程同步问题
    private lateinit var zhProcessor: ZhProcessor

    val readyState: MutableState<Boolean> = mutableStateOf(false)
    var isReady: Boolean
        get() = readyState.value
        set(value) {
            readyState.value = value
        }

    val speedState: MutableState<Float> = mutableStateOf(1.0f)
    var speed: Float
        get() = speedState.value
        set(value) {
            speedState.value = value
            spTtsSpeed = value
        }

    val typeState: MutableState<TtsType> = mutableStateOf(TtsType.FASTSPEECH2)
    var type: TtsType
        get() = typeState.value
        set(value) {
            typeState.value = value
            spTtsType = value
        }

    private var spTtsType: TtsType by App.INSTANCE
        .getSharedPreferences(SP_APP, Context.MODE_PRIVATE)
        .moshiAny(SP_TTS_TYPE, TtsType.FASTSPEECH2)

    private var spTtsSpeed: Float by App.INSTANCE
        .getSharedPreferences(SP_APP, Context.MODE_PRIVATE)
        .moshiAny(SP_TTS_SPEED, 1.0f)

    init {
        type = spTtsType
        speed = spTtsSpeed
    }

    fun initModels(context: Context) {
        zhProcessor = ZhProcessor(context)
        val fastspeechFile = copyAssetFileToDir(context, FASTSPEECH2_NAME, targetDir)
        val tacotronFile = copyAssetFileToDir(context, TACOTRON2_NAME, targetDir)
        val vocoderFile = copyAssetFileToDir(context, MELGAN_NAME, targetDir)
        if (fastspeechFile == null || tacotronFile == null || vocoderFile == null) {
            isReady = false
            return
        }
        fastSpeech = FastSpeech2(fastspeechFile)
        tacotron = Tacotron2(tacotronFile)
        melGan = MBMelGan(vocoderFile)
        isReady = true
    }

    fun stop() {
        inputTextJob?.cancel()
    }

    fun speechAsync(inputText: String, callback: SynthesisCallback) = runBlocking(Dispatchers.Main.immediate) {
        callback.start(TTS_SAMPLE_RATE, TTS_AUDIO_FORMAT, 1)
        if (inputText.isBlank()) {
            callback.done()
            return@runBlocking
        }
        val regex = Regex("[\n，。？?！!,;；]")
        val sentences = inputText.split(regex).filter { it.isNotBlank() }
        inputTextJob = scope.launch {
            sentences.map {
                sentenceToData(it)
            }.forEach { audio ->
                if (audio != null) {
                    writeToCallBack(callback, audio)
                }
            }
        }
        inputTextJob?.join()
        callback.done()
    }

    private fun sentenceToData(sentence: String): FloatArray? {
        val startTime = System.currentTimeMillis()
        val inputIds: IntArray = zhProcessor.text2ids(sentence)
        val tensorOutput: TensorBuffer? = when (type) {
            TtsType.FASTSPEECH2 -> fastSpeech?.getMelSpectrogram(inputIds, speed)
            TtsType.TACOTRON2 -> tacotron?.getMelSpectrogram(inputIds)
        }
        val encoderTime = System.currentTimeMillis()
        //Logger.i("Encoder Time cost=${encoderTime - startTime}")
        if (tensorOutput != null) {
            val audioArray: FloatArray? = try {
                melGan?.getAudio(tensorOutput)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val vocoderTime = System.currentTimeMillis()
            return audioArray
            //Logger.i("Vocoder Time cost=${vocoderTime - encoderTime}")
        }
        return null
    }

    private suspend fun writeToCallBack(callback: SynthesisCallback, audioFloat: FloatArray) {
        val audio = convertTo16Bit(audioFloat)
        //Logger.i("writeToCallBack:Float(${audioFloat.size}) Byte(${audio.size})")
        try {
            val maxBufferSize: Int = callback.maxBufferSize
            var offset = 0
            while (offset < audio.size && currentCoroutineContext().isActive) {
                val bytesToWrite = Math.min(maxBufferSize, audio.size - offset)
                callback.audioAvailable(audio, offset, bytesToWrite)
                offset += bytesToWrite
            }
        } catch (e: Exception) {

        }
    }


    private fun convertTo16Bit(data: FloatArray): ByteArray {
        val byte16bit = ByteArray(data.size shl 1)
        for (i in data.indices) {
            val temp = (32768 * data[i]).toInt()
            byte16bit[i * 2] = temp.toByte()
            byte16bit[i * 2 + 1] = (temp shr 8).toByte()
        }
        return byte16bit
    }

}