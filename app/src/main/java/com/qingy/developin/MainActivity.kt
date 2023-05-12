package com.qingy.developin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.qingy.developin.databinding.ActivityMainBinding
import com.qingy.screen_record.CameraToMpegTest
import com.qingy.screen_record.ScreenRecordHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var recordHelper: ScreenRecordHelper? = null

    companion object {
        val TAG = "MainActivity"
    }

    private fun getActivity(): Activity {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionChecker()
        recordHelper = ScreenRecordHelper(getActivity())
        binding.per.setOnClickListener {
            val list = getunAuthPer()
            if (list.isEmpty()) {
                return@setOnClickListener
            }
            permissionReq(list)
        }
        binding.record.setOnClickListener { recordScreen() }
        binding.recordStop.setOnClickListener { recordStop() }
        binding.cameraToMp4.setOnClickListener { CameraToMpegTest().apply { testEncodeCameraToMp4() } }
    }

    override fun onResume() {
        super.onResume()
        permissionChecker()
    }

    private fun getunAuthPer(): List<String> {
        val list = XXPermissions.getDenied(
            getActivity(),
            Permission.RECORD_AUDIO,
            Permission.MANAGE_EXTERNAL_STORAGE,
            Permission.CAMERA
        )
        return list
    }

    private fun permissionChecker() {
        val list = getunAuthPer()
        binding.hasPer.text = "当前还未授权的权限：$list"
    }

    private fun permissionReq(list: List<String>) {
        XXPermissions.with(getActivity())
            .permission(list)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {

                }

                override fun onDenied(
                    permissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {

                }
            })
    }

    private fun recordScreen() {
        val list = getunAuthPer()
        if (!list.isEmpty()) {
            permissionReq(list)
            return
        }
        realRecord()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        recordHelper?.onActivityResult(requestCode, resultCode, data)
    }

    private fun realRecord() {
        recordHelper?.startRecord()
    }

    private fun recordStop() {
        recordHelper?.stopRecord()
    }

}