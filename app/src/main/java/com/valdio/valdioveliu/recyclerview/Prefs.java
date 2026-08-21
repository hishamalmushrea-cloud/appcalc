package com.valdio.valdioveliu.recyclerview;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private static final String FILE = "daftar";

    private Prefs() {
    }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static String get(Context c, String key, String def) {
        return sp(c).getString(key, def);
    }

    public static void put(Context c, String key, String value) {
        sp(c).edit().putString(key, value).apply();
    }

    public static boolean getBool(Context c, String key, boolean def) {
        return sp(c).getBoolean(key, def);
    }

    public static void putBool(Context c, String key, boolean value) {
        sp(c).edit().putBoolean(key, value).apply();
    }

    public static int getInt(Context c, String key, int def) {
        return sp(c).getInt(key, def);
    }

    public static void putInt(Context c, String key, int value) {
        sp(c).edit().putInt(key, value).apply();
    }
}
