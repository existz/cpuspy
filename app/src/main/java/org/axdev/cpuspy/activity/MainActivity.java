//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.activity;

// imports

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import com.crashlytics.android.Crashlytics;

import com.nispok.snackbar.Snackbar;

import org.axdev.cpuspy.CpuSpyApp;
import org.axdev.cpuspy.CpuStateMonitor;
import org.axdev.cpuspy.CpuStateMonitor.CpuState;
import org.axdev.cpuspy.CpuStateMonitor.CpuStateMonitorException;
import org.axdev.cpuspy.fragments.WhatsNewDialog;
import org.axdev.cpuspy.listeners.ShakeEventListener;
import org.axdev.cpuspy.utils.CPUUtils;
import org.axdev.cpuspy.utils.ThemeUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.TypefaceSpan;
import org.axdev.cpuspy.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

import io.fabric.sdk.android.Fabric;

/** main activity class */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, OnClickListener
{
    // main ui views
    @InjectView(R.id.btn_charged) Button mChargedButton;
    @InjectView(R.id.btn_welcome) Button mWelcomeButton;
    @InjectView(R.id.card_view_charged) CardView mChargedCardView;
    @InjectView(R.id.card_view_states) CardView mStatesCardView;
    @InjectView(R.id.card_view_welcome) CardView mWelcomeCardView;
    @InjectView(R.id.card_view_time) CardView mTimeCardView;
    @InjectView(R.id.btn_info) ImageButton mInfoButton;
    @InjectView(R.id.img_show) ImageView mShowImage;
    @InjectView(R.id.ui_main_layout) LinearLayout mMainLayout;
    @InjectView(R.id.ui_states_view) LinearLayout mStatesView;
    @InjectView(R.id.ui_charged_view) LinearLayout mChargedView;
    @InjectView(R.id.ui_states_warning) LinearLayout mStatesWarning;
    @InjectView(R.id.additional_layout) LinearLayout mAdditionalLayout;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout mSwipeLayout;
    @InjectView(R.id.ui_additional_states) TextView mAdditionalStates;
    @InjectView(R.id.ui_additional_states_show) TextView mAdditionalStatesShow;
    @InjectView(R.id.ui_additional_states_hide) TextView mAdditionalStatesHide;
    @InjectView(R.id.ui_total_state_time) TextView mTotalStateTime;
    @InjectView(R.id.ui_header_total_state_time) TextView mHeaderTotalStateTime;
    @InjectView(R.id.cpu_core_toolbar) Toolbar mToolbar;

    @Optional @InjectView(R.id.main_reveal) View mMainReveal;
    @Optional @InjectView(R.id.ripple_main) MaterialRippleLayout mMaterialRippleLayout;

    @InjectView(R.id.ui_cpu_freq0) TextView mCore0;
    @InjectView(R.id.ui_cpu_freq1) TextView mCore1;
    @InjectView(R.id.ui_cpu_freq2) TextView mCore2;
    @InjectView(R.id.ui_cpu_freq3) TextView mCore3;

    private final String TAG = "CpuSpy";
    private final String WELCOME_SCREEN = "welcomeScreenShown";

    private final Handler mHandler = new Handler();

    private Editor editor;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    private SharedPreferences sp;

    private boolean mMonitorCpu0;
    private boolean mMonitorCpu1;
    private boolean mMonitorCpu2;
    private boolean mMonitorCpu3;

    /** whether or not auto refresh is enabled */
    private boolean mAutoRefresh = false;

    /** lets us know if the battery is fully charged or not */
    private boolean mIsCharged = false;

    /** Initialize the Activity */
    @Override public void onCreate(Bundle savedInstanceState)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThemeUtils.onActivityCreateSetNavBar(this);
        }
        ThemeUtils.onActivityCreateSetTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ButterKnife.inject(this);
        this.setThemeAttributes();
        this.setTypeface();
        this.setAnimation();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();

        /** Use custom Typeface for action bar title on KitKat devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getSupportActionBar() != null && mToolbar != null) {
                getSupportActionBar().setTitle(R.string.app_name_long);
                getSupportActionBar().setElevation(0);
                mToolbar.setElevation(getResources().getDimension(R.dimen.ab_elevation));
            }
        } else {
            final SpannableString s = new SpannableString(getResources().getString(R.string.app_name_long));
            s.setSpan(new TypefaceSpan(this, TypefaceHelper.MEDIUM_FONT), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(s);
            }
        }

        /** Show WhatsNewDialog if versionCode has changed */
        int currentVersionNumber = 0;
        int savedVersionNumber = sp.getInt("version_number", 0);
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception ignored) {}

        if (currentVersionNumber > savedVersionNumber) {
            final WhatsNewDialog newFragment = new WhatsNewDialog();
            newFragment.show(getFragmentManager(), "whatsnew");
            editor.putInt("version_number", currentVersionNumber);
            editor.commit();
        }

        /** Remove welcome cardview if its already been shown */
        boolean welcomeScreenShown = sp.getBoolean(WELCOME_SCREEN, true);
        if (!welcomeScreenShown) { this.removeWelcomeCard(); }

        /** Set colors and listener for SwipeRefreshLayout */
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(getResources().getColor(android.R.color.white));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primary));

        /** Set onClickListener for all buttons */
        mChargedButton.setOnClickListener(this);
        mInfoButton.setOnClickListener(this);
        mWelcomeButton.setOnClickListener(this);
        mStatesCardView.setOnClickListener(this);

        /** Add listener for shake to refresh */
        if (!mAutoRefresh) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mSensorListener = new ShakeEventListener();
                mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
                    @Override
                    public void onShake() {
                        refreshData();
                    }
                });
            }
        }

        /** Register receiver to check battery status */
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override public void onStart () {
        super.onStart();
        this.checkView();
        this.startCoreMonitor();

        // Check to see if autoRefresh is enabled or not
        if (sp.getBoolean("autoRefresh", true)) {
            mAutoRefresh = true;
            mHandler.post(refreshAuto);
        } else {
            mAutoRefresh = false;
        }
    }

    /** Disable handler when activity loses focus */
    @Override public void onPause () {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);

        if (!mAutoRefresh) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mSensorManager.unregisterListener(mSensorListener);
            }
        }
    }

    /** Update the view when the application regains focus */
    @Override public void onResume () {
        super.onResume();

        // Recreate activity if navigation bar or theme changes
        if (PrefsActivity.mThemeChanged) {
            this.startActivity(new Intent(this, this.getClass()));
            this.finish();
            this.overridePendingTransition(0, 0);
            PrefsActivity.mThemeChanged = false;
        } else {
            if (!mAutoRefresh) { refreshData(); }
        }

        // Initialize and start automatic crash reporting
        if(sp.getBoolean("crashReport", true)) {
            Fabric.with(this, new Crashlytics());
        }

        // Register listener for shake to refresh
        if (!mAutoRefresh) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mSensorManager.registerListener(mSensorListener,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(false);
                refreshData();
            }
        }, 1950);
    }

    /** Check to see if the device is fully charged or not */
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int percent = (level * 100) / scale;
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            mIsCharged = percent >= 97 && isCharging;

            if (sp.getBoolean("autoReset", true) || mIsCharged) {
                if (!mAutoRefresh) { checkView(); }
            }
        }
    };

    private void checkView() {
        final File timeInState = new File("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
        boolean mStatesNotFound = !timeInState.exists() || timeInState.length() == 0;

        // Reset timers and show info when battery is charged
        if (sp.getBoolean("autoReset", true) && mIsCharged) {
            mStatesWarning.setVisibility(View.GONE);
            mStatesCardView.setVisibility(View.GONE);
            mTimeCardView.setVisibility(View.GONE);
            mWelcomeCardView.setVisibility(View.GONE);
            mChargedView.setVisibility(View.VISIBLE);

            resetTimers();
        } else {
            mStatesWarning.setVisibility(View.GONE);
            mChargedView.setVisibility(View.GONE);
            mStatesCardView.setVisibility(View.VISIBLE);
            mTimeCardView.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMainReveal.setVisibility(View.GONE);
            }
        }

        // show warning label if no states found
        if (mStatesNotFound) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary_warning)));
            }

            // set navigation and status bar colors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark_warning));
                getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark_warning));
            }

            mMainLayout.setBackgroundColor(getResources().getColor(R.color.primary_warning));

            mToolbar.setVisibility(View.GONE);
            mTimeCardView.setVisibility(View.GONE);
            mWelcomeCardView.setVisibility(View.GONE);
            mStatesCardView.setVisibility(View.GONE);
            mChargedView.setVisibility(View.GONE);
            mStatesWarning.setVisibility(View.VISIBLE);

            // Disable core monitoring
            mHandler.removeCallbacksAndMessages(monitorCpu);
        }
    }

    /** Set UI elements for dark and light themes */
    private void setThemeAttributes() {
        final ColorStateList dark = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_dark));
        final ColorStateList light = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_light));

        // Set background for cards based on selected theme
        mStatesCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));
        mTimeCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));
        mChargedCardView.setCardBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.card_dark_background : R.color.card_light_background));
        mAdditionalLayout.setBackgroundColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.layout_dark_background : R.color.layout_light_background));

        // Set color for drawables based on selected theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mShowImage.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
            mInfoButton.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
        } else {
            final Drawable showDrawable = DrawableCompat.wrap(mShowImage.getDrawable());
            mShowImage.setImageDrawable(showDrawable);
            DrawableCompat.setTintList(showDrawable, (ThemeUtils.DARKTHEME ? dark : light));

            final Drawable infoDrawable = DrawableCompat.wrap(mInfoButton.getDrawable());
            mInfoButton.setImageDrawable(infoDrawable);
            DrawableCompat.setTintList(infoDrawable, (ThemeUtils.DARKTHEME ? dark : light));

            mMaterialRippleLayout.setRippleColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                    R.color.ripple_material_dark : R.color.ripple_material_light));
        }
    }

    /** Apply custom typeface to textviews */
    private void setTypeface() {
        final Typeface mediumFont = TypefaceHelper.get(this, TypefaceHelper.MEDIUM_FONT);
        final TextView mWelcomeSummary = (TextView) findViewById(R.id.welcome_summary);
        final TextView mWelcomeFeatures = (TextView) findViewById(R.id.welcome_features);

        mWelcomeSummary.setTypeface(mediumFont);
        mWelcomeFeatures.setTypeface(mediumFont);
        mAdditionalStatesShow.setTypeface(mediumFont);
        mAdditionalStatesHide.setTypeface(mediumFont);
        mHeaderTotalStateTime.setTypeface(mediumFont);
    }

    /** Animate cardview sliding up from bottom */
    private void setAnimation(){
        final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);

        slideUp.setDuration(500);
        mStatesCardView.startAnimation(slideUp);
        mTimeCardView.startAnimation(slideUp);
    }

    /** Remove welcome cardview after first launch */
    private void removeWelcomeCard() {
        final ViewGroup mViewGroup = (ViewGroup) mWelcomeCardView.getParent();
        if (mViewGroup != null) {
            mViewGroup.removeView(mWelcomeCardView);
        }
    }

    /** Reset the cpu timers */
    public static void resetTimers() {
        try {
            CpuSpyApp.getCpuStateMonitor().setOffsets();
        } catch (CpuStateMonitorException e) {
            // TODO: something
        }
        CpuSpyApp.saveOffsets();
    }

    /** Restore the cpu timers */
    public static void restoreTimers() {
        CpuSpyApp.getCpuStateMonitor().removeOffsets();
        CpuSpyApp.saveOffsets();
    }

    /** Global On click listener for all views */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_info:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    // Prevent circle reveal from being cut off
                    mAutoRefresh = false;

                    int cx = mMainLayout.getRight();
                    int cy = mMainLayout.getTop();

                    float finalRadius = (float) Math.hypot(mMainLayout.getWidth(), mMainLayout.getHeight());

                    // Create a reveal {@link Animator} that starts clipping the view from
                    // the top right corner until the whole view is covered.
                    final Animator anim =
                            ViewAnimationUtils.createCircularReveal(mMainReveal, cx, cy, 0, Math.round(finalRadius));

                    // Set animation duration
                    anim.setDuration(325);
                    // Set a natural ease-in/ease-out interpolator.
                    anim.setInterpolator(new AccelerateDecelerateInterpolator());
                    // Set a listener for when the animation starts and ends
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mMainReveal.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                            overridePendingTransition(0, 0);
                        }
                    });

                    // Finally start the animation
                    anim.start();
                } else {
                    startActivity(new Intent(this, InfoActivity.class));
                }
                break;
            case R.id.btn_welcome:
                this.removeWelcomeCard();
                editor.putBoolean(WELCOME_SCREEN, false);
                editor.commit();
                break;
            case R.id.btn_charged:
                editor.putBoolean("autoReset", false);
                editor.commit();
                refreshData();
                resetTimers();
                break;
            case R.id.card_view_states:
                if (mAdditionalStatesShow.isShown()) {
                    final AnimationSet animSet = new AnimationSet(true);
                    animSet.setInterpolator(new DecelerateInterpolator());
                    animSet.setFillAfter(true);
                    animSet.setFillEnabled(true);

                    final RotateAnimation animRotate = new RotateAnimation(0.0f, 180.0f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);

                    animRotate.setDuration(300);
                    animRotate.setFillAfter(true);
                    animSet.addAnimation(animRotate);

                    mShowImage.startAnimation(animSet);

                    mAdditionalStatesShow.setVisibility(View.GONE);
                    mAdditionalStatesHide.setVisibility(View.VISIBLE);
                    mAdditionalStates.setVisibility(View.VISIBLE);
                } else {
                    final AnimationSet animSet = new AnimationSet(true);
                    animSet.setInterpolator(new DecelerateInterpolator());
                    animSet.setFillAfter(true);
                    animSet.setFillEnabled(true);

                    final RotateAnimation animRotate = new RotateAnimation(-180.0f, 0f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);

                    animRotate.setDuration(500);
                    animRotate.setFillAfter(true);
                    animSet.addAnimation(animRotate);

                    mShowImage.startAnimation(animSet);

                    mAdditionalStatesShow.setVisibility(View.VISIBLE);
                    mAdditionalStatesHide.setVisibility(View.GONE);
                    mAdditionalStates.setVisibility(View.GONE);
                }
                break;
        }
    }

    private final Runnable monitorCpu = new Runnable() {
        final File cpu0 = new File(CPUUtils.CPU0);
        final File cpu1 = new File(CPUUtils.CPU1);
        final File cpu2 = new File(CPUUtils.CPU2);
        final File cpu3 = new File(CPUUtils.CPU3);

        public void run() {
            /** Set the frequency for CPU0 */
            if(mMonitorCpu0) {
                try {
                    if (cpu0.length() == 0) {
                        // CPU0 should never be empty
                        mCore0.setText(null);
                        Log.e(TAG, "Problem getting CPU cores");
                        return;
                    } else {
                        mCore0.setText(CPUUtils.getCpu0() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    //DO SOMETHING
                }
            }

            /** Set the frequency for CPU1 */
            if(mMonitorCpu1) {
                try {
                    if (cpu1.length() == 0) {
                        mCore1.setText(R.string.core_offline);
                    } else {
                        mCore1.setText(CPUUtils.getCpu1() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    // DO SOMETHING
                }
            }

            /** Set the frequency for CPU2 */
            if(mMonitorCpu2) {
                try {
                    if (cpu2.length() == 0) {
                        mCore2.setText(R.string.core_offline);
                    } else {
                        mCore2.setText(CPUUtils.getCpu2() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    // DO SOMETHING
                }
            }

            /** Set the frequency for CPU3 */
            if(mMonitorCpu3) {
                try {
                    if (cpu3.length() == 0) {
                        mCore3.setText(R.string.core_offline);
                    } else {
                        mCore3.setText(CPUUtils.getCpu3() + "MHz");
                    }
                } catch (NumberFormatException ignored) {
                    //DO SOMETHING
                }
            }

            mHandler.postDelayed(monitorCpu, 1000); // 1 second
        }
    };

    /** Check which CPU cores to start monitoring */
    private void startCoreMonitor() {
        switch (CPUUtils.getCoreCount()) {
            case 1:
                mMonitorCpu0 = true;
                mMonitorCpu1 = false;
                mMonitorCpu2 = false;
                mMonitorCpu3 = false;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.GONE);
                mCore2.setVisibility(View.GONE);
                mCore3.setVisibility(View.GONE);
                break;
            case 2:
                mMonitorCpu0 = true;
                mMonitorCpu1 = true;
                mMonitorCpu2 = false;
                mMonitorCpu3 = false;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.GONE);
                mCore3.setVisibility(View.GONE);
                break;
            case 3:
                mMonitorCpu0 = true;
                mMonitorCpu1 = true;
                mMonitorCpu2 = true;
                mMonitorCpu3 = false;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.VISIBLE);
                mCore3.setVisibility(View.GONE);
                break;
            case 4:
                mMonitorCpu0 = true;
                mMonitorCpu1 = true;
                mMonitorCpu2 = true;
                mMonitorCpu3 = true;

                mCore0.setVisibility(View.VISIBLE);
                mCore1.setVisibility(View.VISIBLE);
                mCore2.setVisibility(View.VISIBLE);
                mCore3.setVisibility(View.VISIBLE);
                break;
        }
        mHandler.post(monitorCpu);
    }

    /** called when we want to inflate the menu */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // request inflater from activity and inflate into its menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // made it
        return true;
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
            case R.id.menu_reset:
                resetTimers();
                this.updateView();
                Snackbar.with(getApplicationContext())
                        .text(R.string.snackbar_text_reset) // text to display
                        .actionLabel(R.string.action_dismiss) // action button label
                        .actionColor(Color.parseColor("#f4b400"))
                        .show(this);
                mStatesCardView.setVisibility(View.GONE);
                break;
            case R.id.menu_restore:
                restoreTimers();
                this.updateView();
                Snackbar.with(getApplicationContext())
                        .text(R.string.snackbar_text_restore) // text to display
                        .actionLabel(R.string.action_dismiss) // action button label
                        .actionColor(Color.parseColor("#f4b400"))
                        .show(this);
                break;
            case R.id.menu_settings:
                this.startActivity(new Intent(this, PrefsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Generate and update all UI elements */
    private void updateView() {
        /** Get the CpuStateMonitor from the app, and iterate over all states,
         * creating a row if the duration is > 0 or otherwise marking it in
         * extraStates (missing) */

        final CpuStateMonitor monitor = CpuSpyApp.getCpuStateMonitor();
        mStatesView.removeAllViews();
        List<String> extraStates = new ArrayList<>();
        for (CpuState state : monitor.getStates()) {
            if (state.duration > 0) {
                generateStateRow(state, mStatesView);
            } else {
                if (state.freq == 0) {
                    extraStates.add("Deep Sleep");
                } else {
                    extraStates.add(state.freq / 1000 + "MHz");
                }
            }
        }

        // update the total state time
        long totTime = monitor.getTotalStateTime() / 100;
        mTotalStateTime.setText(sToString(totTime));

        // for all the 0 duration states, add the the Unused State area
        if (extraStates.size() > 0) {
            int n = 0;
            String str = "";

            for (String s : extraStates) {
                if (n++ > 0)
                    str += ", ";
                str += s;
            }
            mAdditionalStates.setText(str);
        } else {
            mAdditionalStates.setText("Empty");
        }
    }

    /** Attempt to update the time-in-state info */
    private void refreshData() {
        this.checkView();
        new RefreshStateDataTask().execute((Void) null);
    }

    /** @return A nicely formatted String representing tSec seconds */
    private static String sToString(long tSec) {
        long h = (long)Math.floor(tSec / (60*60));
        long m = (long)Math.floor((tSec - h*60*60) / 60);
        long s = tSec % 60;
        String sDur;
        sDur = h + ":";
        if (m < 10)
            sDur += "0";
        sDur += m + ":";
        if (s < 10)
            sDur += "0";
        sDur += s;

        return sDur;
    }

    /**
     * @return a View that correpsonds to a CPU freq state row as specified
     * by the state parameter
     */
    private View generateStateRow(CpuState state, ViewGroup parent) {
        // inflate the XML into a view in the parent
        final LayoutInflater inf = LayoutInflater.from(getApplicationContext());
        final RelativeLayout theRow = (RelativeLayout)inf.inflate(
                R.layout.state_row, parent, false);

        // what percentage we've got
        final CpuStateMonitor monitor = CpuSpyApp.getCpuStateMonitor();
        float per = (float)state.duration * 100 /
                monitor.getTotalStateTime();
        String sPer = String.format("%.01f", per) + "%";

        // state name
        String sFreq;
        if (state.freq == 0) {
            sFreq = "Deep Sleep";
        } else {
            sFreq = state.freq / 1000 + "MHz";
        }

        // duration
        long tSec = state.duration / 100;
        String sDur = sToString(tSec);

        // map UI elements to objects
        final TextView mFreqText = (TextView)theRow.findViewById(R.id.ui_freq_text);
        final TextView mDurText = (TextView)theRow.findViewById(R.id.ui_duration_text);
        final TextView mPerText = (TextView)theRow.findViewById(R.id.ui_percentage_text);
        final ProgressBar mBar = (ProgressBar)theRow.findViewById(R.id.ui_bar);

        // Set UI elements for dark and light themes
        mFreqText.setTextColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.primary_text_color_dark : R.color.primary_text_color));
        mDurText.setTextColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.primary_text_color_dark : R.color.primary_text_color));
        mPerText.setTextColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                R.color.primary_text_color_dark : R.color.primary_text_color));
        mBar.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), ThemeUtils.DARKTHEME ?
                R.drawable.progess_drawable_dark : R.drawable.progess_drawable, null));

        // modify the row
        mFreqText.setText(sFreq);
        mPerText.setText(sPer);
        mDurText.setText(sDur);
        mBar.setProgress(Math.round(per));

        // add it to parent and return
        parent.addView(theRow);
        return theRow;
    }

    /** Keep updating the state data off the UI thread for slow devices */
    private class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {

        /** Stuff to do on a seperate thread */
        @Override protected Void doInBackground(Void... v) {
            final CpuStateMonitor monitor = CpuSpyApp.getCpuStateMonitor();
            try {
                monitor.updateStates();
            } catch (CpuStateMonitorException e) {
                Log.e(TAG, "Problem getting CPU states");
            }

            return null;
        }

        /** Executed on the UI thread right before starting the task */
        @Override protected void onPreExecute() {}

        /** Executed on UI thread after task */
        @Override protected void onPostExecute(Void v) {
            updateView();
        }
    }

    /** Update data every 1 second if auto refresh is enabled */
    private final Runnable refreshAuto = new Runnable() {
        public void run() {
            if(mAutoRefresh) {
                refreshData();
                mHandler.postDelayed(refreshAuto, 1000); // 1 second
            }
        }
    };

    @Override protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        this.unregisterReceiver(this.mBatInfoReceiver); // unregister receiver
    }
}
