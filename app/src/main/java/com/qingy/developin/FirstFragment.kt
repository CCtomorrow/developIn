package com.qingy.developin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ShellUtils
import com.qingy.developin.databinding.FragmentFirstBinding
import com.qingy.util.KLog
import java.io.IOException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private val TAG = "FirstFragment"
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        //直接执行adb shell指令

        //val execCmd = ShellUtils.execCmd("tcpip 5555", false, true)
        val execCmd = ShellUtils.execCmd("ip addr show wlan0", false, true)
        KLog.e(TAG, "execCmd:$execCmd")
        binding.textviewFirst.text = execCmd.toString()

        val execRootCmd = execRootCmd("ip addr show wlan0")
        KLog.e(TAG, "execCmd:$execRootCmd")

    }

    fun execRootCmd(cmd: String) {
        var cmd = cmd
        var result = -1
        var content = ""
        try {
            cmd = cmd.replace("adb shell", "")
            val process = Runtime.getRuntime().exec(cmd)
            KLog.d(TAG, "process $process")
            content = process.toString()
            KLog.d(TAG, "content $content")
            result = process.waitFor()
            KLog.d(TAG, "result $result")
        } catch (e: IOException) {
            KLog.d(TAG, "exception $e")
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}