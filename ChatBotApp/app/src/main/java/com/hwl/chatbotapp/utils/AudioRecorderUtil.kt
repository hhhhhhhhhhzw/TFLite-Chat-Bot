package com.hwl.chatbotapp.utils

import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException

object AudioRecorderUtil {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String = ""

    fun startRecording() {
        val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val audioFile = File(outputDir, "recorded_audio.mp3")
        audioFilePath = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    fun getAudioFilePath(): String {
        return audioFilePath
    }
}
