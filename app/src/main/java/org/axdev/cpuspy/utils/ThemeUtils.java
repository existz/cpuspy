package org.axdev.cpuspy.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import org.axdev.cpuspy.R;

public class ThemeUtils extends ActionBarActivity {

    public final static int NAVBAR_DEFAULT = 0;
    public final static int NAVBAR_COLORED = 1;

    public final static int LIGHT = 0;
    public final static int DARK = 1;

    public static void changeToTheme(Activity activity, int theme) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("theme", theme);
        editor.commit();

        activity.startActivity(new Intent(activity, activity.getClass()));

        activity.finish();
    }

    public static void changeNavBar(Activity activity, int navbar) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("navbar", navbar);
        editor.commit();

        activity.startActivity(new Intent(activity, activity.getClass()));

        activity.finish();
    }

    public static void onActivityCreateSetTheme(Activity activity) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        int mTheme = sp.getInt("theme", 0);

        switch (mTheme) {
            default:
            case LIGHT:
                activity.setTheme(R.style.AppTheme);
                //actionBar.setBackgroundDrawable(context.getResources().getDrawable(R.color.primary));
                activity.getWindow().getDecorView().setBackgroundColor(activity.getResources().getColor(R.color.light_background));
                editor.putBoolean("darkTheme", false);
                editor.commit();
                break;
            case DARK:
                activity.setTheme(R.style.AppThemeDark);
                //actionBar.setBackgroundDrawable(context.getResources().getDrawable(R.color.primary));
                activity.getWindow().getDecorView().setBackgroundColor(activity.getResources().getColor(R.color.dark_background));
                editor.putBoolean("darkTheme", true);
                editor.commit();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void onActivityCreateSetNavBar(Activity activity) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        int mNavBar = sp.getInt("navbar", 0);

        switch (mNavBar) {
            default:
            case NAVBAR_DEFAULT:
                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(android.R.color.black));
                break;
            case NAVBAR_COLORED:
                activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.primary_dark));
                break;
        }
    }
}