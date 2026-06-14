package com.akrep.xdownloader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;

public class CookieUtils {
    private static final String PREF_NAME = "XCookies";
    private static final String KEY_COOKIES = "cookies";
    private static final String KEY_LOGGED_IN = "is_logged_in";

    public static void saveCookies(Context context, String cookies) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_COOKIES, cookies)
                .putBoolean(KEY_LOGGED_IN, true)
                .apply();
    }

    public static String getCookies(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COOKIES, "");
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public static void logout(Context context) {
        CookieManager.getInstance().removeAllCookies(null);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
