package com.qingy.screen_record

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.widget.Toast
import com.qingy.util.GlobalUtils
import com.qingy.util.KLog
import java.io.File
import java.nio.ByteBuffer

/**
 *
 * <b>Package:</b> com.qingy.screen_record <br>
 * <b>Create Date:</b> 2023/5/10 <br>
 * <b>@author:</b> qingyong <br>
 * <b>Address:</b> qingyong@grgbanking.com <br>
 * <b>Description:</b>  <br>
 */
class NormalRecord {
    private val TAG = "NormalRecord"
    private val VIDEO_FRAME_RATE = 30
    private var mediaRecorder: MediaRecorder? = null

    private var mediaProjection: MediaProjection? = null
    private lateinit var displayMetrics: DisplayMetrics
    private lateinit var savePath: String

    private var virtualDisplay: VirtualDisplay? = null

    private var isRecording = false
    private var recordAudio = false

    private fun showToast(str: String) {
        Toast.makeText(
            GlobalUtils.getApp(),
            str,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun startRecord(
        displayMetrics: DisplayMetrics,
        mediaProjection: MediaProjection,
        path: String
    ) {
        this.displayMetrics = displayMetrics
        this.mediaProjection = mediaProjection
        this.savePath = path
        if (initRecorder()) {
            isRecording = true
            mediaRecorder?.start()
        } else {
            showToast("当前手机暂不支持录屏")
        }
    }

    private fun initRecorder(): Boolean {
        KLog.d(TAG, "initRecorder")
        var result = true
        val width = Math.min(displayMetrics.widthPixels, 1080)
        val height = Math.min(displayMetrics.heightPixels, 1920)
        KLog.d(
            TAG,
            "width:${displayMetrics.widthPixels} height:${displayMetrics.heightPixels} w:$width h:$height"
        )
        mediaRecorder = MediaRecorder()
        mediaRecorder?.apply {
            if (recordAudio) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            if (recordAudio) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            }
            setOutputFile(savePath)
            setVideoSize(width, height)
            setVideoEncodingBitRate(8388608)
            setVideoFrameRate(VIDEO_FRAME_RATE)
            try {
                prepare()
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "MainScreen", width, height, displayMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null
                )
                KLog.d(TAG, "initRecorder 成功")
            } catch (e: Exception) {
                KLog.e(TAG, "IllegalStateException preparing MediaRecorder: ${e.message}")
                e.printStackTrace()
                result = false
            }
        }
        return result
    }

    /**
     * if you has parameters, the recordAudio will be invalid
     */
    fun stopRecord(
        videoDuration: Long = 0,
        audioDuration: Long = 0,
        afdd: AssetFileDescriptor? = null
    ) {
        stop()
        if (audioDuration != 0L && afdd != null) {
            syntheticAudio(videoDuration, audioDuration, afdd)
        } else {
            // saveFile
            val file = File(savePath)
            if (file.exists()) {
                val newFile = File(savePath.replace(".tmp", ".mp4"))
                file.renameTo(newFile)
                refreshVideo(newFile)
            }
        }
    }

    private fun stop() {
        if (isRecording) {
            isRecording = false
            try {
                mediaRecorder?.apply {
                    setOnErrorListener(null)
                    setOnInfoListener(null)
                    setPreviewDisplay(null)
                    stop()
                    KLog.d(TAG, "stop success")
                }
            } catch (e: Exception) {
                KLog.e(TAG, "stopRecorder() error！${e.message}")
            } finally {
                mediaRecorder?.reset()
                virtualDisplay?.release()
                mediaProjection?.stop()
            }
        }
    }

    /**
     * https://stackoverflow.com/questions/31572067/android-how-to-mux-audio-file-and-video-file
     */
    @SuppressLint("WrongConstant")
    private fun syntheticAudio(
        audioDuration: Long,
        videoDuration: Long,
        afdd: AssetFileDescriptor
    ) {
        KLog.d(TAG, "start syntheticAudio")
        val newFile = File(savePath.replace(".tmp", ".mp4"))
        if (newFile.exists()) {
            newFile.delete()
        }
        try {
            newFile.createNewFile()
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(savePath)
            val audioExtractor = MediaExtractor()
            afdd.apply {
                audioExtractor.setDataSource(
                    fileDescriptor,
                    startOffset,
                    length * videoDuration / audioDuration
                )
            }
            val muxer =
                MediaMuxer(newFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            videoExtractor.selectTrack(0)
            val videoFormat = videoExtractor.getTrackFormat(0)
            val videoTrack = muxer.addTrack(videoFormat)

            audioExtractor.selectTrack(0)
            val audioFormat = audioExtractor.getTrackFormat(0)
            val audioTrack = muxer.addTrack(audioFormat)

            var sawEOS = false
            var frameCount = 0
            val offset = 100
            val sampleSize = 1000 * 1024
            val videoBuf = ByteBuffer.allocate(sampleSize)
            val audioBuf = ByteBuffer.allocate(sampleSize)
            val videoBufferInfo = MediaCodec.BufferInfo()
            val audioBufferInfo = MediaCodec.BufferInfo()

            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            muxer.start()

            // 每秒多少帧
            // 实测 OPPO R9em 垃圾手机，拿出来的没有 MediaFormat.KEY_FRAME_RATE
            val frameRate = if (videoFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
            } else {
                31
            }
            // 得出平均每一帧间隔多少微妙
            val videoSampleTime = 1000 * 1000 / frameRate
            while (!sawEOS) {
                videoBufferInfo.offset = offset
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset)
                if (videoBufferInfo.size < 0) {
                    sawEOS = true
                    videoBufferInfo.size = 0
                } else {
                    videoBufferInfo.presentationTimeUs += videoSampleTime
                    videoBufferInfo.flags = videoExtractor.sampleFlags
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo)
                    videoExtractor.advance()
                    frameCount++
                }
            }
            var sawEOS2 = false
            var frameCount2 = 0
            while (!sawEOS2) {
                frameCount2++
                audioBufferInfo.offset = offset
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset)

                if (audioBufferInfo.size < 0) {
                    sawEOS2 = true
                    audioBufferInfo.size = 0
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    audioBufferInfo.flags = audioExtractor.sampleFlags
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo)
                    audioExtractor.advance()
                }
            }
            muxer.stop()
            muxer.release()
            videoExtractor.release()
            audioExtractor.release()

            // 删除无声视频文件
            File(savePath).takeIf { it.exists() }.let { it?.delete() }
        } catch (e: Exception) {
            KLog.e(TAG, "Mixer Error:${e.message}")
            // 视频添加音频合成失败，直接保存视频
            File(savePath).takeIf { it.exists() }.let { it?.renameTo(newFile) }
        } finally {
            afdd.close()
            refreshVideo(newFile)
        }
    }

    fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
        }
    }

    fun pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
        }
    }

    fun clearAll() {
        mediaRecorder?.release()
        mediaRecorder = null
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
    }

    private fun refreshVideo(newFile: File) {
        KLog.d(TAG, "screen record end,file length:${newFile.length()}.")
        if (newFile.length() > 5000) {
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(newFile)
            GlobalUtils.getApp().sendBroadcast(intent)
            showToast("保存成功")
        } else {
            newFile.delete()
            showToast("当前手机暂不支持录屏")
            KLog.e(TAG, "录制失败")
        }
    }

}