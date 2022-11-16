package com.qingy.util

import android.util.Log

/**
 *
 * Copyright (C), 2014-2022, qingy
 *
 * <b>Project:</b> DevelopIn <br>
 * <b>Package:</b> com.qingy.util <br>
 * <b>Create Date:</b> 2022/11/16 <br>
 * <b>@author:</b> qingy <br>
 * <b>Address:</b> qingyongai@gmail.com <br>
 * <b>Description:</b>  <br>
 */
object GLog {

    /**
     * 追溯调用此Log的代码的堆栈数量
     */
    var STACK_TRACE_INDEX = 4

    /**
     * 日志tag前缀
     */
    var TAG_DEFAULT = "GLog"

    /**
     * 因为logcat的输出是有限的，这里做限制，最大输出1000个字符
     */
    var MAX_LENGTH = 1000

    /**
     * 是否显示日志
     */
    var SHOW_LOG = true

    fun d(tag: String?, vararg msg: String?): Int {
        return println(Log.DEBUG, tag, null, *msg)
    }

    fun d(tag: String?, vararg msg: String, tr: Throwable?): Int {
        return println(Log.DEBUG, tag, tr, *msg)
    }

    fun i(tag: String?, vararg msg: String?): Int {
        return println(Log.INFO, tag, null, *msg)
    }

    fun i(tag: String?, vararg msg: String, tr: Throwable?): Int {
        return println(Log.INFO, tag, tr, *msg)
    }

    fun w(tag: String?, vararg msg: String?): Int {
        return println(Log.WARN, tag, null, *msg)
    }

    fun w(tag: String?, vararg msg: String, tr: Throwable?): Int {
        return println(Log.WARN, tag, tr, *msg)
    }

    fun e(tag: String?, vararg msg: String?): Int {
        return println(Log.ERROR, tag, null, *msg)
    }

    fun e(tag: String?, vararg msg: String, tr: Throwable?): Int {
        return println(Log.ERROR, tag, tr, *msg)
    }

    private const val PARAM = "Param"
    private const val NULL = "null"

    private fun println(type: Int, tagStr: String?, tr: Throwable?, vararg objects: Any?): Int {
        val stackTrace = Thread.currentThread().stackTrace
        //修改溢出问题
        var index: Int = STACK_TRACE_INDEX + 1
        if (index >= stackTrace.size - 1) {
            index = stackTrace.size - 1
        }

        val targetElementP = stackTrace[index]
        val targetElement = stackTrace[index - 1]

        val fileNameP = targetElementP.fileName
        val methodNameP = targetElementP.methodName
        var lineNumberP = targetElementP.lineNumber
        if (lineNumberP < 0) {
            lineNumberP = 0
        }

        val fileName = targetElement.fileName
        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber
        if (lineNumber < 0) {
            lineNumber = 0
        }

        val tag = "$TAG_DEFAULT-$tagStr"
        var msg = if (objects.size > 1) {
            val sb = StringBuilder()
            sb.append("\n")
            for (i in objects.indices) {
                val item = objects[i]
                if (item == null) {
                    sb.append(PARAM)
                        .append("[")
                        .append(i)
                        .append("]")
                        .append(" = ")
                        .append(NULL)
                        .append("\n")
                } else {
                    sb.append(PARAM)
                        .append("[")
                        .append(i)
                        .append("]")
                        .append(" = ")
                        .append(item.toString())
                        .append("\n")
                }
            }
            sb.toString()
        } else if (objects.size == 1) {
            objects[0]?.toString() ?: NULL
        } else {
            NULL
        }

        //msg = objects.contentToString()

        tr?.let {
            msg += Log.getStackTraceString(it)
        }
        val headStringP = "[ ($fileNameP:$lineNumberP)#$methodNameP ] "
        val headString = "[ ($fileName:$lineNumber)#$methodName ] "

        return convertToAlogPrintln(type, tag, headString + headStringP + msg)
    }

    private fun convertToAlogPrintln(type: Int, tag: String?, msg: String): Int {
        var index = 0
        val length = msg.length
        val countOfSub: Int = length / MAX_LENGTH
        return if (countOfSub > 0) {
            for (i in 0 until countOfSub) {
                val sub = msg.substring(index, index + MAX_LENGTH)
                realPrintln(type, tag, sub)
                index += MAX_LENGTH
            }
            realPrintln(type, tag, msg.substring(index, length))
        } else {
            realPrintln(type, tag, msg)
        }
    }

    /**
     * 真正输出
     */
    private fun realPrintln(type: Int, tag: String?, msg: String): Int {
        return if (!SHOW_LOG) {
            -1
        } else when (type) {
            Log.VERBOSE -> Log.v(tag, msg)
            Log.DEBUG -> Log.d(tag, msg)
            Log.INFO -> Log.i(tag, msg)
            Log.WARN -> Log.w(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
            else -> Log.wtf(tag, msg)
        }
    }

}
