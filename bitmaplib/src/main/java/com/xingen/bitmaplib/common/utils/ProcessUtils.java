package com.xingen.bitmaplib.common.utils;

import android.os.Looper;

/**
 * Created by ${新根} on 2018/7/15.
 * blog博客:http://blog.csdn.net/hexingen
 */

public class ProcessUtils {

    public static void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("需要在主线程中调用");
        }
    }
}
