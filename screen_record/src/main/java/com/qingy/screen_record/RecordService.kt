package com.qingy.screen_record

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.Toast
import com.qingy.util.KLog
import java.io.File
import java.nio.ByteBuffer

/**
 *
 * <b>Package:</b> com.qingy.screen_record <br>
 * <b>Create Date:</b> 2023/5/9 <br>
 * <b>@author:</b> qingyong <br>
 * <b>Address:</b> qingyong@grgbanking.com <br>
 * <b>Description:</b>  <br>
 */
class RecordService : Service() {
    private val TAG = "RecordService"
    private val VIDEO_FRAME_RATE = 30
    private val mediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val displayMetrics by lazy { resources.displayMetrics }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private var mediaRecorder: MediaRecorder? = null

    private var saveFile: File? = null
    private var isRecording = false
    private var recordAudio = false

    private var savePath: String = Environment.getExternalStorageDirectory().absolutePath +
            File.separator + "DCIM" + File.separator + "Camera"
    private val saveName: String = "sr_${System.currentTimeMillis()}"

    override fun onCreate() {
        super.onCreate()
        setUpAsForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        KLog.d(TAG, "other action:${intent?.action}")
        when (intent?.action) {
            "start" -> {
                startRecord(intent)
            }
            "stop" -> {
                stopRecord()
            }
            "pause" -> {
                pause()
            }
            "resume" -> {
                resume()
            }
            else -> {
                KLog.e(TAG, "other action:${intent?.action}")
            }
        }
        return onStartCommand
    }

    private fun startRecord(intent: Intent) {
        mediaProjection = mediaProjectionManager?.getMediaProjection(
            intent.getIntExtra("code", 0),
            intent.getParcelableExtra<Intent>("data")!!
        )
        if (initRecorder()) {
            isRecording = true
            mediaRecorder?.start()
        } else {
            showToast("当前手机暂不支持录屏")
        }
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
            if (saveFile != null) {
                val newFile = File(savePath, "$saveName.mp4")
                // 录制结束后修改后缀为 mp4
                saveFile!!.renameTo(newFile)
                refreshVideo(newFile)
            }
            saveFile = null
        }
    }

    private fun initRecorder(): Boolean {
        KLog.d(TAG, "initRecorder")
        var result = true
        val f = File(savePath)
        if (!f.exists()) {
            f.mkdirs()
        }
        saveFile = File(savePath, "$saveName.tmp")
        saveFile?.apply {
            if (exists()) {
                delete()
            }
        }
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
            setOutputFile(saveFile!!.absolutePath)
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

    private fun initRecordAd() {
        KLog.d(TAG, "initRecorder")
        var result = true
        val f = File(savePath)
        if (!f.exists()) {
            f.mkdirs()
        }
        saveFile = File(savePath, "$saveName.tmp")
        saveFile?.apply {
            if (exists()) {
                delete()
            }
        }
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        KLog.d(TAG, "width:${width} height:${height}")

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

    private fun resume() {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            mediaRecorder?.resume()
        }
    }

    private fun pause() {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
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
        val newFile = File(savePath, "$saveName.mp4")
        if (newFile.exists()) {
            newFile.delete()
        }
        try {
            newFile.createNewFile()
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(saveFile!!.absolutePath)
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
            saveFile?.delete()
        } catch (e: Exception) {
            KLog.e(TAG, "Mixer Error:${e.message}")
            // 视频添加音频合成失败，直接保存视频
            saveFile?.renameTo(newFile)
        } finally {
            afdd.close()
            handler.post {
                refreshVideo(newFile)
                saveFile = null
            }
        }
    }

    private fun refreshVideo(newFile: File) {
        KLog.d(TAG, "screen record end,file length:${newFile.length()}.")
        if (newFile.length() > 5000) {
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(newFile)
            sendBroadcast(intent)
            showToast("保存成功")
        } else {
            newFile.delete()
            showToast("当前手机暂不支持录屏")
            KLog.e(TAG, "录制失败")
        }
    }

    private fun showToast(str: String) {
        Toast.makeText(
            this,
            str,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setUpAsForeground() {
        val id = "screen_record_service"
        val name = id
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id, name, NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            channel.enableVibration(false)
            notificationManager?.createNotificationChannel(channel)
        }
        val builder = Notification.Builder(this.applicationContext)
        builder.setContentIntent(PendingIntent.getActivity(this, 0, Intent(), FLAG_IMMUTABLE))
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher))
            .setContentTitle("录屏服务")
            .setContentText("录屏服务正在运行")
            .setWhen(System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(id)
        }
        val notification = builder.build()
        startForeground(10, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

}