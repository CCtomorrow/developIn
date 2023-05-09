package com.qingy.util;

import android.util.Log;

/**
 * Copyright (C), 2015-2022, qingy
 *
 * <b>Project:</b> KeepAlive <br>
 * <b>Package:</b> com.boolbird.keepalive <br>
 * <b>Create Date:</b> 2022/11/17 <br>
 * <b>@author:</b> qingy <br>
 * <b>Address:</b> qingyongai@gmail.com <br>
 * <b>Description:</b>  <br>
 */
public class KLog {

    /**
     * 追溯调用此Log的代码的堆栈数量
     */
    public static int STACK_TRACE_INDEX = 4;

    /**
     * 日志tag前缀
     */
    public static String TAG_DEFAULT = "KLog";

    /**
     * 因为logcat的输出是有限的，这里做限制，最大输出1000个字符
     */
    public static int MAX_LENGTH = 1000;

    /**
     * 是否显示日志
     */
    public static boolean SHOW_LOG = true;

    public static int d(String tag, String... msg) {
        return println(Log.DEBUG, tag, null, msg);
    }

    public static int d(String tag, Throwable tr, String... msg) {
        return println(Log.DEBUG, tag, tr, msg);
    }

    public static int i(String tag, String... msg) {
        return println(Log.INFO, tag, null, msg);
    }

    public static int i(String tag, Throwable tr, String... msg) {
        return println(Log.INFO, tag, tr, msg);
    }

    public static int w(String tag, String... msg) {
        return println(Log.WARN, tag, null, msg);
    }

    public static int w(String tag, Throwable tr, String... msg) {
        return println(Log.WARN, tag, tr, msg);
    }

    public static int e(String tag, String... msg) {
        return println(Log.ERROR, tag, null, msg);
    }

    public static int e(String tag, Throwable tr, String... msg) {
        return println(Log.ERROR, tag, tr, msg);
    }

    private static final String PARAM = "Param";
    private static final String NULL = "null";

    private static int println(int type, String tagStr, Throwable tr, String... msg) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        //修改溢出问题
        int index = STACK_TRACE_INDEX + 1;
        if (index >= stackTrace.length - 1) {
            index = stackTrace.length - 1;
        }
        StackTraceElement targetElementP = stackTrace[index];
        StackTraceElement targetElement = stackTrace[index - 1];
        String fileNameP = targetElementP.getFileName();
        String methodNameP = targetElementP.getMethodName();
        int lineNumberP = targetElementP.getLineNumber();
        if (lineNumberP < 0) {
            lineNumberP = 0;
        }
        String fileName = targetElement.getFileName();
        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();
        if (lineNumber < 0) {
            lineNumber = 0;
        }
        String tag = TAG_DEFAULT + "-" + tagStr;
        String message = getObjectsString(msg);
        if (tr != null) {
            message += "\n" + Log.getStackTraceString(tr);
        }
        String headStringP = "[ (" + fileNameP + ":" + lineNumberP + ")#" + methodNameP + " ] ";
        String headString = "[ (" + fileName + ":" + lineNumber + ")#" + methodName + " ] ";
        return convertToAlogPrintln(type, tag, headString + headStringP + message);
    }

    public static String getObjectsString(String... objects) {
        if (objects == null) {
            return NULL;
        }
        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                String object = objects[i];
                if (object == null) {
                    stringBuilder.append(PARAM).append("[").append(i).append("]")
                            .append(" = ").append(NULL).append("\n");
                } else {
                    stringBuilder.append(PARAM).append("[").append(i).append("]")
                            .append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else if (objects.length == 1) {
            String object = objects[0];
            return object == null ? NULL : object.toString();
        } else {
            return NULL;
        }
    }

    /**
     * 转换成安卓log输出
     */
    public static int convertToAlogPrintln(int type, String tag, String msg) {
        int index = 0;
        int length = msg.length();
        int countOfSub = length / MAX_LENGTH;
        if (countOfSub > 0) {
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + MAX_LENGTH);
                realPrintln(type, tag, sub);
                index += MAX_LENGTH;
            }
            return realPrintln(type, tag, msg.substring(index, length));
        } else {
            return realPrintln(type, tag, msg);
        }
    }

    /**
     * 真正输出
     */
    private static int realPrintln(int type, String tag, String msg) {
        if (!SHOW_LOG) {
            return -1;
        }
        switch (type) {
            case Log.VERBOSE:
                return Log.v(tag, msg);
            case Log.DEBUG:
                return Log.d(tag, msg);
            case Log.INFO:
                return Log.i(tag, msg);
            case Log.WARN:
                return Log.w(tag, msg);
            case Log.ERROR:
                return Log.e(tag, msg);
            default:
                return Log.wtf(tag, msg);
        }
    }

}
