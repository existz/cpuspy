package org.axdev.cpuspy.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.internal.ThemeSingleton;

import org.axdev.cpuspy.R;

import java.util.Calendar;

/**
 * @author Aidan Follestad (afollestad)
 * @author Rob Beane (existz) - Auto Theme
 */
public abstract class ThemedActivity extends AppCompatActivity {

    public static boolean mIsDarkTheme;
    private boolean mLastDarkTheme;
    private boolean mLastColoredNav;
    private int mLastPrimaryColor;
    private int mLastAccentColor;
    private final static int LIGHT = 0;
    private final static int DARK = 1;
    private final static int AUTO = 2;

    protected int darkTheme() {
        mIsDarkTheme = true;
        return R.style.AppThemeDark;
    }

    protected int lightTheme() {
        mIsDarkTheme = false;
        return R.style.AppTheme;
    }

    protected boolean isDarkTheme() {
        int mTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt("theme", 0);

        switch (mTheme) {
            default:
            case LIGHT:
                return false;
            case DARK:
                return true;
            case AUTO:
                Calendar c = Calendar.getInstance();
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
                return !(timeOfDay >= 6 && timeOfDay < 20);
        }
    }


    public int primaryColor() {
        final int defaultColor = ContextCompat.getColor(this, R.color.primary);
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("primary_color", defaultColor);
    }

    protected void primaryColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("primary_color", newColor).commit();
    }

    public int primaryColorDark() {
        return CircleView.shiftColorDown(primaryColor());
    }

    public int accentColor() {
        final int defaultColor = ContextCompat.getColor(this, R.color.primary);
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("accent_color", defaultColor);
    }

    protected void accentColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("accent_color", newColor).commit();
    }

    protected boolean isColoredNavBar() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("coloredNavBar", true);
    }

    protected boolean hasColoredBars() {
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLastDarkTheme = isDarkTheme();
        mLastColoredNav = isColoredNavBar();
        mLastPrimaryColor = primaryColor();
        mLastAccentColor = accentColor();
        ColorStateList sl = ColorStateList.valueOf(mLastAccentColor);
        ThemeSingleton.get().positiveColor = sl;
        ThemeSingleton.get().neutralColor = sl;
        ThemeSingleton.get().negativeColor = sl;
        ThemeSingleton.get().widgetColor = mLastAccentColor;
        setTheme(mLastDarkTheme ? darkTheme() : lightTheme());
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Sets color of entry in the system recents page
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.icon),
                    primaryColor());
            setTaskDescription(td);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hasColoredBars()) {
            final int dark = primaryColorDark();
            getWindow().setStatusBarColor(dark);
            if (mLastColoredNav) {
                getWindow().setNavigationBarColor(dark);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean darkTheme = isDarkTheme();
        boolean coloredNav = isColoredNavBar();
        int primaryColor = primaryColor();
        int accentColor = accentColor();
        if (darkTheme != mLastDarkTheme || coloredNav != mLastColoredNav
                || primaryColor != mLastPrimaryColor || accentColor != mLastAccentColor) {
            recreate();
        }
    }
}