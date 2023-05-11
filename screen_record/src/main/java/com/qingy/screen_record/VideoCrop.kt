package com.qingy.screen_record

import android.graphics.Rect
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodec.CONFIGURE_FLAG_ENCODE
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
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
class VideoCrop(
    private val virtualDisplay: VirtualDisplay?,
    private val path: String,
    private val rect: Rect
) {

    companion object {
        private val TAG = "VideoCrop"
    }

    private val handlerThread = HandlerThread("VideoCrop")
    private val handler by lazy { Handler(handlerThread.looper) }

    private var videoEncoder: VideoEncoder
    private var mVideoTrackIndex = -1
    private var running = false

    init {
        handlerThread.start()
        videoEncoder = VideoEncoder(path, rect)
        val mediaCodec = videoEncoder.mediaCodec
        val mediaMuxer = videoEncoder.mediaMuxer
        val encoderThread = videoEncoder.handlerThread
        encoderThread.start()
        //mediaCodec?.setCallback(videoEncoder.encoderCallback, Handler(encoderThread.looper))
        mediaCodec?.configure(videoEncoder.mediaFormat, null, null, CONFIGURE_FLAG_ENCODE)
        videoEncoder.surface = mediaCodec?.createInputSurface()
        virtualDisplay?.surface = videoEncoder.surface

        handler.post {
            mediaCodec?.apply {
                start()
                running = true
                val bufferInfo = MediaCodec.BufferInfo()
                KLog.d(TAG, "start:${Thread.currentThread().name}")
                while (running) {
                    val bufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
                    KLog.d(TAG, "bufferId:$bufferId")
                    if (bufferId >= 0) {
                        mediaCodec.getOutputBuffer(bufferId)?.apply {
                            // write head info
                            if (mVideoTrackIndex == -1) {
                                KLog.d(TAG, "this is first frame, call writeHeadInfo first")
                                mVideoTrackIndex = writeHeadInfo(this, bufferInfo)
                            }
                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                                KLog.d(TAG, "write outputBuffer")
                                mediaMuxer?.writeSampleData(mVideoTrackIndex, this, bufferInfo)
                            }
                        }
                        releaseOutputBuffer(bufferId, false)
                    }
                }
                KLog.d(TAG, "stop")
                stop()
                release()
                virtualDisplay?.release()
                videoEncoder.mediaMuxer?.stop()
                val newFile = File(path.replace(".tmp", ".mp4"))
                File(path).takeIf { it.exists() }.let { it?.renameTo(newFile) }
            }
        }
    }

    private fun writeHeadInfo(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo): Int {
        val csd = ByteArray(bufferInfo.size)
        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
        outputBuffer.position(bufferInfo.offset)
        outputBuffer.get(csd)
        var sps: ByteBuffer? = null
        var pps: ByteBuffer? = null
        for (i in bufferInfo.size - 1 downTo 4) {
            if (csd[i].toInt() == 1 && csd[i - 1].toInt() == 0 && csd[i - 2].toInt() == 0 && csd[i - 3].toInt() == 0) {
                sps = ByteBuffer.allocate(i - 3)
                pps = ByteBuffer.allocate(bufferInfo.size - (i - 3))
                sps.put(csd, 0, i - 3).position(0)
                pps.put(csd, i - 3, bufferInfo.size - (i - 3)).position(0)
            }
        }
        var ret = -1
        videoEncoder.mediaCodec?.outputFormat?.apply {
            if (sps != null && pps != null) {
                setByteBuffer("csd-0", sps)
                setByteBuffer("csd-1", pps)
            }
            ret = videoEncoder.mediaMuxer?.addTrack(this) ?: -1
            KLog.d(TAG, "videoTrackIndex: $ret")
            videoEncoder.mediaMuxer?.start()
        }
        return ret
    }

    fun stop() {
        running = false
    }

}