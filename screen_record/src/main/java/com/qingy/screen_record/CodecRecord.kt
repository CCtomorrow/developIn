package com.qingy.screen_record

import android.graphics.Rect
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.qingy.util.KLog

/**
 *
 * <b>Package:</b> com.qingy.screen_record <br>
 * <b>Create Date:</b> 2023/5/10 <br>
 * <b>@author:</b> qingyong <br>
 * <b>Address:</b> qingyong@grgbanking.com <br>
 * <b>Description:</b>  <br>
 */
class CodecRecord : BaseRecord() {
    private val TAG = "CodecRecord"

    private var videoCrop: VideoCrop? = null

    fun startRecord(
        displayMetrics: DisplayMetrics,
        mediaProjection: MediaProjection,
        path: String
    ) {
        this.displayMetrics = displayMetrics
        this.mediaProjection = mediaProjection
        this.savePath = path
        initRecorder()
    }

    private fun initRecorder() {
        KLog.d(TAG, "initRecorder")
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        KLog.d(TAG, "width:${displayMetrics.widthPixels} height:${displayMetrics.heightPixels}")
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "MainScreen", width, height, displayMetrics.densityDpi,
            VIRTUAL_DISPLAY_FLAG_PUBLIC, null, null, null
        )
        //virtualDisplay?.surface = xxx //后续再设置
        //开始录制
        videoCrop = VideoCrop(virtualDisplay, savePath, Rect(0, 0, width, height))
    }

    fun stopRecord() {
        videoCrop?.stop()
    }

    fun resume() {
    }

    fun pause() {
    }
}