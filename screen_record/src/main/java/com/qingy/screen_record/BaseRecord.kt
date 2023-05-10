package com.qingy.screen_record

import android.content.Intent
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.net.Uri
import android.util.DisplayMetrics
import android.widget.Toast
import com.qingy.util.GlobalUtils
import com.qingy.util.KLog
import java.io.File

/**
 *
 * <b>Package:</b> com.qingy.screen_record <br>
 * <b>Create Date:</b> 2023/5/10 <br>
 * <b>@author:</b> qingyong <br>
 * <b>Address:</b> qingyong@grgbanking.com <br>
 * <b>Description:</b>  <br>
 */
abstract class BaseRecord {

    private val TAG = "CodecRecord"

    protected var mediaProjection: MediaProjection? = null
    protected lateinit var displayMetrics: DisplayMetrics
    protected lateinit var savePath: String

    protected var virtualDisplay: VirtualDisplay? = null

    protected var isRecording = false
    protected var recordAudio = false

    protected fun showToast(str: String) {
        Toast.makeText(
            GlobalUtils.getApp(),
            str,
            Toast.LENGTH_SHORT
        ).show()
    }

    protected fun refreshVideo(newFile: File) {
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