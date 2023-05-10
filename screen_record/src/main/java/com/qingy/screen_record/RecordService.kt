package com.qingy.screen_record

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.qingy.util.KLog
import java.io.File

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
    private val mediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }
    private var mediaProjection: MediaProjection? = null

    private var normalRecord: NormalRecord? = null

    private val displayMetrics by lazy { resources.displayMetrics }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private var saveFile: File? = null

    private var savePath: String = Environment.getExternalStorageDirectory().absolutePath +
            File.separator + "DCIM" + File.separator + "Camera"
    private val saveName: String = "sr_${System.currentTimeMillis()}"

    override fun onCreate() {
        super.onCreate()
        normalRecord = NormalRecord()
        setUpAsForeground()
    }

//    fun getVirtualDisplay(): VirtualDisplay? {
//        return virtualDisplay
//    }

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
        //创建文件存储路径
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
        mediaProjection?.let {
            normalRecord?.startRecord(displayMetrics, it, saveFile!!.absolutePath)
        }
    }

    /**
     * if you has parameters, the recordAudio will be invalid
     */
    fun stopRecord() {
        normalRecord?.stopRecord()
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

//        virtualDisplay = mediaProjection?.createVirtualDisplay(
//            "MainScreen", width, height, displayMetrics.densityDpi,
//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, null, null, null
//        )

        //virtualDisplay?.surface = xxx //后续再设置

        //开始录制
        //val videoCrop = VideoCrop(this, saveFile!!.absolutePath, Rect(0, 0, width, height))

    }

    private fun resume() {
        normalRecord?.resume()
    }

    private fun pause() {
        normalRecord?.pause()
    }

    private fun setUpAsForeground() {
        val id = "screen_record_service"
        val name = id
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
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
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            builder.setChannelId(id)
        }
        val notification = builder.build()
        startForeground(10, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

}