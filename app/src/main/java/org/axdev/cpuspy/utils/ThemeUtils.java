package org.axdev.cpuspy.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.axdev.cpuspy.R;

import java.util.Calendar;

public class ThemeUtils extends AppCompatActivity {

    public final static int NAVBAR_DEFAULT = 0;
    public final static int NAVBAR_COLORED = 1;

    public final static int LIGHT = 0;
    public final static int DARK = 1;
    public final static int AUTO = 2;
    public static boolean darkTheme;
    public static boolean coloredNavBar;

    public static void changeToTheme(Activity activity, int mTheme) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        final Editor editor = sp.edit();

        editor.putInt("theme", mTheme).apply();

        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(0, 0);
    }

    public static void changeNavBar(Activity activity, int mNavBar) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        final Editor editor = sp.edit();

        editor.putInt("navbar", mNavBar).apply();

        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(0, 0);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);

        int mTheme = sp.getInt("theme", 0);

        switch (mTheme) {
            default:
            case LIGHT:
                activity.setTheme(R.style.AppTheme);
                darkTheme = false;
                break;
            case DARK:
                activity.setTheme(R.style.AppThemeDark);
                darkTheme = true;
                break;
            case AUTO:
                Calendar c = Calendar.getInstance();
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

                if (timeOfDay >= 6 && timeOfDay < 20) {
                    activity.setTheme(R.style.AppTheme);
                    darkTheme = false;
                } else {
                    activity.setTheme(R.style.AppThemeDark);
                    darkTheme = true;
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void onActivityCreateSetNavBar(Activity activity) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);

        int mNavBar = sp.getInt("navbar", 0);

        switch (mNavBar) {
            default:
            case NAVBAR_DEFAULT:
                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(android.R.color.black));
                coloredNavBar = false;
                break;
            case NAVBAR_COLORED:
                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.primary_dark));
                coloredNavBar = true;
                break;
        }
    }
}