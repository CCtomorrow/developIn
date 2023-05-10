package com.qingy.screen_record

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.qingy.util.KLog

/**
 * 录屏帮助类，仅限 Android 5.0 及以上使用
 *
 * Author: nanchen
 * Date: 2019/6/21 15:19
 */
class ScreenRecordHelper @JvmOverloads constructor(private var activity: Activity) {

    private val mediaProjectionManager by lazy {
        activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }

    fun startRecord() {
        if (mediaProjectionManager == null) {
            KLog.e(TAG, "mediaProjectionManager == null，当前手机暂不支持录屏")
            showToast("当前手机暂不支持录屏")
            return
        }
        KLog.d(TAG, "start record")
        mediaProjectionManager?.apply {
            val intent = this.createScreenCaptureIntent()
            if (activity.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                ) != null
            ) {
                activity.startActivityForResult(intent, REQUEST_CODE)
            } else {
                showToast("当前手机暂不支持录屏")
            }
        }
    }

    fun resume() {
        val service = Intent(activity, RecordService::class.java)
        service.action = "resume"
        ContextCompat.startForegroundService(activity, service)
    }

    fun pause() {
        val service = Intent(activity, RecordService::class.java)
        service.action = "pause"
        ContextCompat.startForegroundService(activity, service)
    }

    fun stopRecord() {
        val service = Intent(activity, RecordService::class.java)
        service.action = "stop"
        ContextCompat.startForegroundService(activity, service)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val service = Intent(activity, RecordService::class.java)
                service.putExtra("code", resultCode)
                service.putExtra("data", data)
                //service.putExtra("action", "start")
                service.action = "start"
                ContextCompat.startForegroundService(activity, service)
            } else {
                showToast("当前手机暂不支持录屏")
            }
        }
    }

    private fun showToast(resId: String) {
        Toast.makeText(
            activity.applicationContext,
            resId,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val VIDEO_FRAME_RATE = 30
        private const val REQUEST_CODE = 1024
        private const val TAG = "ScreenRecordHelper"
    }

}