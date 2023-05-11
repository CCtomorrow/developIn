package com.qingy.screen_record

import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
import android.media.MediaCodecList
import android.media.MediaCodecList.REGULAR_CODECS
import android.media.MediaFormat
import android.media.MediaFormat.*
import android.media.MediaMuxer
import android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
import android.os.HandlerThread
import android.view.Surface
import com.qingy.util.KLog

/**
 *
 * <b>Package:</b> com.qingy.screen_record <br>
 * <b>Create Date:</b> 2023/5/10 <br>
 * <b>@author:</b> qingyong <br>
 * <b>Address:</b> qingyong@grgbanking.com <br>
 * <b>Description:</b>  <br>
 */
class VideoEncoder(private val path: String, private val rect: Rect) {

    companion object {
        private val TAG = "VideoEncoder"
    }

    var mediaFormat: MediaFormat? = null
    var mediaCodec: MediaCodec? = null
    var mediaMuxer: MediaMuxer? = null
    var surface: Surface? = null

    val handlerThread = HandlerThread("VideoEncoder")
    val encoderCallback = EncoderCallback()

    public class EncoderCallback : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val inputBuffer = codec.getInputBuffer(index)
            KLog.d(TAG, "onInputBufferAvailable")
        }

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            KLog.d(TAG, "onOutputBufferAvailable")
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            KLog.d(TAG, "onError")
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            KLog.d(TAG, "onOutputFormatChanged")
        }

    }

    init {
        val width = rect.width()
        val height = rect.height()
        var codec: MediaCodec? = null
        //h264
        val createVideoFormat = createVideoFormat(MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(KEY_COLOR_FORMAT, COLOR_FormatSurface)
            setInteger(KEY_BIT_RATE, width * height * 4)
            setInteger(KEY_FRAME_RATE, 60)
            setInteger(KEY_I_FRAME_INTERVAL, 3)
        }
        MediaCodecList(REGULAR_CODECS).apply {
            findDecoderForFormat(createVideoFormat)?.apply {
                codec = MediaCodec.createByCodecName(this)
            }
        }
        takeIf { codec == null }.apply {
            codec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC)
        }
        val muxer = MediaMuxer(path, MUXER_OUTPUT_MPEG_4)

        mediaFormat = createVideoFormat
        mediaCodec = codec
        mediaMuxer = muxer
    }

}