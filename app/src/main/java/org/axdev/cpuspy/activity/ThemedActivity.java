package org.axdev.cpuspy.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.internal.ThemeSingleton;

import org.axdev.cpuspy.R;

import java.util.Calendar;

import butterknife.BindColor;
import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 * @author Rob Beane (existz) - Auto Theme
 */
public abstract class ThemedActivity extends AppCompatActivity {

    @BindColor(R.color.material_blue_500) int mMaterialBlue500;

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
        if (isLightAB(this) && !isLightAccent(this)) {
            return R.style.AppThemeDark_LightAB;
        } else if (!isLightAB(this) && isLightAccent(this)) {
            return R.style.AppThemeDark_LightAccent;
        } else if (isLightAB(this) && isLightAccent(this)) {
            return R.style.AppThemeDark_LightAB_LightAccent;
        } else {
            return R.style.AppThemeDark;
        }
    }

    protected int lightTheme() {
        mIsDarkTheme = false;
        if (isLightAB(this) && !isLightAccent(this)) {
            return R.style.AppTheme_LightAB;
        } else if (!isLightAB(this) && isLightAccent(this)) {
            return R.style.AppTheme_LightAccent;
        } else if (isLightAB(this) && isLightAccent(this)) {
            return R.style.AppTheme_LightAB_LightAccent;
        } else {
            return R.style.AppTheme;
        }
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
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("primary_color", mMaterialBlue500);
    }

    protected void primaryColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("primary_color", newColor).commit();
    }

    public int primaryColorDark() {
        return CircleView.shiftColorDown(primaryColor());
    }

    public int accentColor() {
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("accent_color", mMaterialBlue500);
    }

    protected void accentColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("accent_color", newColor).commit();
    }

    protected boolean isColoredNavBar() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("coloredNavBar", false);
    }

    protected boolean hasColoredBars() {
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ButterKnife.bind(this, this);
        mLastDarkTheme = isDarkTheme();
        mLastColoredNav = isColoredNavBar();
        mLastPrimaryColor = primaryColor();
        mLastAccentColor = accentColor();
        ColorStateList sl = ColorStateList.valueOf(mLastAccentColor);
        ThemeSingleton.get().positiveColor = sl;
        ThemeSingleton.get().neutralColor = sl;
        ThemeSingleton.get().negativeColor = sl;
        ThemeSingleton.get().widgetColor = mLastAccentColor;
        //noinspection ResourceType
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

    public static boolean isLightAB(Context context) {
        int primaryColor = PreferenceManager.getDefaultSharedPreferences(context).getInt("primary_color", 0);
        return primaryColor == -328966 || primaryColor == -657932 || primaryColor == -1118482
                || primaryColor == -2039584 || primaryColor == -1249295 || primaryColor == -2034959
                || primaryColor == -657931 || primaryColor == -5138 || primaryColor == -1512714
                || primaryColor == -1838339 || primaryColor == -1968642 || primaryColor == -2033670
                || primaryColor == -1509911 || primaryColor == -919319 || primaryColor == -394265
                || primaryColor == -537 || primaryColor == -1823 || primaryColor == -3104
                || primaryColor == -267801 || primaryColor == -13124 || primaryColor == -2634552
                || primaryColor == -1053719 || primaryColor == -3155748 || primaryColor == -3610935
                || primaryColor == -4987396 || primaryColor == -12846 || primaryColor == -203540
                || primaryColor == -476208 || primaryColor == -793099 || primaryColor == -1185802
                || primaryColor == -3029783 || primaryColor == -3814679 || primaryColor == -4464901
                || primaryColor == -5051406 || primaryColor == -5054501 || primaryColor == 2298424
                || primaryColor == -985917 || primaryColor == -1596 || primaryColor == -2659
                || primaryColor == -4941 || primaryColor == -8062 || primaryColor == -8014
                || primaryColor == -1;
    }

    public static boolean isLightAccent(Context context) {
        int accentColor = PreferenceManager.getDefaultSharedPreferences(context).getInt("accent_color", 0);
        return accentColor == -2298424 || accentColor == -5054501 || accentColor == -5051406
                || accentColor == -985917 || accentColor == -2659 || accentColor == -1596
                || accentColor == -4464901 || accentColor == -8060929 || accentColor == -5767189
                || accentColor == -4589878 || accentColor == -3342448 || accentColor == -721023
                || accentColor == -115 || accentColor == -4941 || accentColor == -8062
                || accentColor == -328966 || accentColor == -657932 || accentColor == -8014
                || accentColor == -1118482 || accentColor == -2039584 || accentColor == -1249295
                || accentColor == -2034959 || accentColor == -657931 || accentColor == -5138
                || accentColor == -1512714 || accentColor == -1838339 || accentColor == -1968642
                || accentColor == -2033670 || accentColor == -1509911 || accentColor == -919319
                || accentColor == -394265 || accentColor == -537 || accentColor == -1823
                || accentColor == -3104 || accentColor == -267801 || accentColor == -13124
                || accentColor == -2634552 || accentColor == -1053719 || accentColor == -3155748
                || accentColor == -3610935 || accentColor == -4987396 || accentColor == -12846
                || accentColor == -203540 || accentColor == -476208 || accentColor == -793099
                || accentColor == -1185802 || accentColor == -3029783 || accentColor == -3814679
                || accentColor == -1;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}