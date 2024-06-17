package com.hwl.chatbotapp.tts

import android.content.Context
import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import android.util.Log
import com.hwl.chatbotapp.common.FASTSPEECH2_NAME
import com.hwl.chatbotapp.common.MELGAN_NAME
import com.hwl.chatbotapp.common.TACOTRON2_NAME
import com.hwl.chatbotapp.common.targetDir
import com.hwl.chatbotapp.utils.copyAssetFileToDir
import com.hwl.chatbotapp.utils.ZhProcessor
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

    fun initModels(context: Context) {
        zhProcessor = ZhProcessor(context)
        val fastspeechFile = copyAssetFileToDir(context, FASTSPEECH2_NAME, targetDir)
        val tacotronFile = copyAssetFileToDir(context, TACOTRON2_NAME, targetDir)
        val vocoderFile = copyAssetFileToDir(context, MELGAN_NAME, targetDir)
        if (fastspeechFile == null || tacotronFile == null || vocoderFile == null) {
            return
        }
        fastSpeech = FastSpeech2(fastspeechFile)
        tacotron = Tacotron2(tacotronFile)
        melGan = MBMelGan(vocoderFile)
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
        val tensorOutput: TensorBuffer? = fastSpeech?.getMelSpectrogram(inputIds, 1.0f)

        val encoderTime = System.currentTimeMillis()
        if (tensorOutput != null) {
            val audioArray: FloatArray? = try {
                melGan?.getAudio(tensorOutput)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val vocoderTime = System.currentTimeMillis()
            return audioArray
        }
        return null
    }

    private suspend fun writeToCallBack(callback: SynthesisCallback, audioFloat: FloatArray) {
        val audio = convertTo16Bit(audioFloat)
        try {
            val maxBufferSize: Int = callback.maxBufferSize
            var offset = 0
            while (offset < audio.size && currentCoroutineContext().isActive) {
                val bytesToWrite = Math.min(maxBufferSize, audio.size - offset)
                callback.audioAvailable(audio, offset, bytesToWrite)
                offset += bytesToWrite
            }
        } catch (e: Exception) {
            Log.e("TAG", "writeToCallBack: ", )
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