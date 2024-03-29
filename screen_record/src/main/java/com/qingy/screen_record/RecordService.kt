package com.qingy.screen_record

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.display.VirtualDisplay
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
import java.text.SimpleDateFormat
import java.util.*

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

    private var recorder: CodecRecord? = null

    private val displayMetrics by lazy { resources.displayMetrics }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private var saveFile: File? = null

    private val dataFormat by lazy { SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()) }
    private val date by lazy { Date() }

    private var savePath: String = Environment.getExternalStorageDirectory().absolutePath +
            File.separator + "DCIM" + File.separator + "Camera"

    override fun onCreate() {
        super.onCreate()
        recorder = CodecRecord()
        setUpAsForeground()
    }

    fun getVirtualDisplay(): VirtualDisplay? {
        return recorder?.virtualDisplay
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
        //创建文件存储路径
        val f = File(savePath)
        if (!f.exists()) {
            f.mkdirs()
        }
        date.time = System.currentTimeMillis()
        val saveName = "sr_${dataFormat.format(date)}"
        saveFile = File(savePath, "$saveName.tmp")
        saveFile?.apply {
            if (exists()) {
                delete()
            }
        }
        mediaProjection?.let {
            recorder?.startRecord(displayMetrics, it, saveFile!!.absolutePath)
        }
    }

    /**
     * if you has parameters, the recordAudio will be invalid
     */
    fun stopRecord() {
        recorder?.stopRecord()
    }

    private fun resume() {
        recorder?.resume()
    }

    private fun pause() {
        recorder?.pause()
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