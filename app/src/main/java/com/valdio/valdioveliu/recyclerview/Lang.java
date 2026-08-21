package com.valdio.valdioveliu.recyclerview;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

public final class Lang {
    public static final String AR = "ar";
    public static final String EN = "en";

    private Lang() {
    }

    public static Context wrap(Context context) {
        String lang = Prefs.get(context, "lang", AR);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static void set(Context context, String lang) {
        Prefs.put(context, "lang", lang);
    }
}
