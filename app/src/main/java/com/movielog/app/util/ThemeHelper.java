package com.movielog.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    private static final String PREF_NAME = "movielog_settings";
    private static final String KEY_THEME = "theme";
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";

    /** SharedPreferences'tan kaydedilen temayı okuyup uygular. */
    public static void applyTheme(Context context) {
        String theme = getSavedTheme(context);
        if (THEME_LIGHT.equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    /** Temayı kaydeder ve uygular. */
    public static void saveAndApplyTheme(Context context, String theme) {
        getPrefs(context).edit().putString(KEY_THEME, theme).apply();
        applyTheme(context);
    }

    public static String getSavedTheme(Context context) {
        return getPrefs(context).getString(KEY_THEME, THEME_DARK);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
