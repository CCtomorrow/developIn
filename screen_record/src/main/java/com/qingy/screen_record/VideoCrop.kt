package com.qingy.screen_record

import android.graphics.Rect
import android.media.MediaCodec.CONFIGURE_FLAG_ENCODE
import android.os.Handler
import android.os.HandlerThread

/**
 *
 * <b>Package:</b> com.qingy.screen_record <br>
 * <b>Create Date:</b> 2023/5/10 <br>
 * <b>@author:</b> qingyong <br>
 * <b>Address:</b> qingyong@grgbanking.com <br>
 * <b>Description:</b>  <br>
 */
class VideoCrop(private val path: String, private val rect: Rect) {

    private val handlerThread = HandlerThread("VideoCrop")
    private val handler by lazy { Handler(handlerThread.looper) }

    private lateinit var videoEncoder: VideoEncoder

    init {
        handlerThread.start()
        videoEncoder = VideoEncoder(path, rect)
        val mediaCodec = videoEncoder.mediaCodec
        val encoderThread = videoEncoder.handlerThread
        encoderThread.start()
        mediaCodec?.setCallback(videoEncoder.encoderCallback, Handler(encoderThread.looper))
        mediaCodec?.configure(videoEncoder.mediaFormat, null, null, CONFIGURE_FLAG_ENCODE)
        videoEncoder.surface = mediaCodec?.createInputSurface()

        handler.post {

        }

    }

}