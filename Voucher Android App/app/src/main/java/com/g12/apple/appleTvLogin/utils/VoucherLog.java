package com.g12.apple.appleTvLogin.utils;

import android.util.Log;

import com.g12.apple.BuildConfig;

public class VoucherLog {

    private static boolean RELEASE_BUILD = !BuildConfig.DEBUG;
    public static void d(String TAG, String log) {
        if (!RELEASE_BUILD)
            Log.d("AppleTv::" + TAG, log);
    }

    public static void w(String TAG, String log) {
        Log.w("AppleTv::" + TAG, log);
    }

    public static void e(String TAG, String log) {
        Log.e("AppleTv::" + TAG, log);
    }

    public static void e(Exception exception) {
        Log.e("AppleTv::", exception.getMessage());
    }
}
