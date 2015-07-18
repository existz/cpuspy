//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

// imports

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import org.axdev.cpuspy.CpuSpyApp;
import org.axdev.cpuspy.CpuState;
import org.axdev.cpuspy.CpuStateMonitor;
import org.axdev.cpuspy.CpuStateMonitor.CpuStateMonitorException;
import org.axdev.cpuspy.activity.PrefsActivity;
import org.axdev.cpuspy.listeners.ShakeEventListener;
import org.axdev.cpuspy.utils.ThemeUtils;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;

/** main activity class */
public class TimerFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnClickListener
{
    // main ui views
    @Bind(R.id.btn_charged) Button mChargedButton;
    @Bind(R.id.btn_welcome) Button mWelcomeCardButton;
    @Bind(R.id.btn_feature) Button mFeatureCardButton;
    @Bind(R.id.card_view_states) CardView mStatesCardView;
    @Bind(R.id.card_view_welcome) CardView mWelcomeCardView;
    @Bind(R.id.card_view_feature) CardView mFeatureCardView;
    @Bind(R.id.card_view_time) CardView mTimeCardView;
    @Bind(R.id.img_show) ImageView mShowImage;
    @Bind(R.id.warning_img) ImageView mWarningImage;
    @Bind(R.id.ui_states_view) LinearLayout mStatesView;
    @Bind(R.id.ui_charged_view) LinearLayout mChargedView;
    @Bind(R.id.ui_states_warning) LinearLayout mStatesWarning;
    @Bind(R.id.card_container) LinearLayout mCardContainer;
    @Bind(R.id.swipe_container) SwipeRefreshLayout mSwipeLayout;
    @Bind(R.id.ui_additional_states) TextView mAdditionalStates;
    @Bind(R.id.ui_additional_states_show) TextView mAdditionalStatesShow;
    @Bind(R.id.ui_additional_states_hide) TextView mAdditionalStatesHide;
    @Bind(R.id.ui_total_state_time) TextView mTotalStateTime;
    @Bind(R.id.ui_header_total_state_time) TextView mHeaderTotalStateTime;
    @Bind(R.id.welcome_summary) TextView mWelcomeCardSummary;
    @Bind(R.id.welcome_features) TextView mWelcomeCardFeatures;
    @Bind(R.id.feature_title) TextView mFeatureCardTitle;

    @Nullable @Bind(R.id.ripple_main) MaterialRippleLayout mMaterialRippleLayout;

    private final String WELCOME_SCREEN = "welcomeScreenShown";
    private final String NEW_FEATURE = "newFeatureShown";

    private final Handler mHandler = new Handler();

    private CpuStateMonitor monitor;
    private Editor editor;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    private SharedPreferences sp;

    private boolean mAutoRefresh;
    private boolean mIsCharged;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.timer_layout, container, false);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sp.edit();
        monitor = CpuSpyApp.getCpuStateMonitor();

        this.setThemeAttributes();
        this.setTypeface();
        this.setCardAnimation();

        /** Show WhatsNewDialog if versionCode has changed */
        int currentVersionNumber = 0;
        int savedVersionNumber = sp.getInt("version_number", 0);
        try {
            PackageInfo pi = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception ignored) {}

        if (currentVersionNumber > savedVersionNumber) {
            final WhatsNewDialog newFragment = new WhatsNewDialog();
            newFragment.show(getActivity().getFragmentManager(), "whatsnew");
            editor.putInt("version_number", currentVersionNumber).apply();
        }

        /** Remove welcome cardview if its already been shown */
        boolean welcomeScreenShown = sp.getBoolean(WELCOME_SCREEN, true);
        if (!welcomeScreenShown) { this.removeView(mWelcomeCardView); }

        /** Remove new feature cardview if its already been shown */
        boolean newFeatureShown = sp.getBoolean(NEW_FEATURE, true);
        if (!newFeatureShown) { this.removeView(mFeatureCardView); }

        /** Set colors and listener for SwipeRefreshLayout */
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(getResources().getColor(android.R.color.white));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.primary));

        /** Set onClickListener for all buttons */
        mChargedButton.setOnClickListener(this);
        mWelcomeCardButton.setOnClickListener(this);
        mStatesCardView.setOnClickListener(this);
        mFeatureCardButton.setOnClickListener(this);

        /** Add listener for shake to refresh */
        if (!mAutoRefresh) {
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
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
        getActivity().registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override public void onStart () {
        super.onStart();
        this.refreshData();

        // Check to see if autoRefresh is enabled or not
        if (sp.getBoolean("autoRefresh", true)) {
            mAutoRefresh = true;
            mHandler.post(refreshAuto);
            mSwipeLayout.setEnabled(false);
        } else {
            mAutoRefresh = false;
            mSwipeLayout.setEnabled(true);
        }
    }

    /** Disable handler when fragment loses focus */
    @Override public void onPause () {
        super.onPause();
        if (!mAutoRefresh) setShakeRefresh(false);
        mAutoRefresh = false;
    }

    /** Update the view when the application regains focus */
    @Override public void onResume () {
        super.onResume();
        // Register listener for shake to refresh
        if (!mAutoRefresh) setShakeRefresh(true);
    }

    @Override public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        }

        // show warning label if no states found
        if (mStatesNotFound) {
            removeView(mTimeCardView);
            removeView(mStatesCardView);
            removeView(mWelcomeCardView);
            removeView(mChargedView);
            mStatesWarning.setVisibility(View.VISIBLE);

            // Disable refreshing methods
            if (!mAutoRefresh) setShakeRefresh(false);
            mAutoRefresh = false;
            mSwipeLayout.setEnabled(false);
        }
    }

    private void setShakeRefresh(boolean enabled) {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            if (enabled) {
                mSensorManager.registerListener(mSensorListener,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_UI);
            } else {
                mSensorManager.unregisterListener(mSensorListener);
            }
        }
    }

    /** Set UI elements for dark and light themes */
    private void setThemeAttributes() {
        final ColorStateList dark = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_dark));
        final ColorStateList light = ColorStateList.valueOf(getResources().getColor(R.color.drawable_color_light));

        // Set color for drawables based on selected theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mShowImage.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
            mWarningImage.setImageTintList(ThemeUtils.DARKTHEME ? dark : light);
        } else {
            final Drawable showDrawable = DrawableCompat.wrap(mShowImage.getDrawable());
            mShowImage.setImageDrawable(showDrawable);
            DrawableCompat.setTintList(showDrawable, (ThemeUtils.DARKTHEME ? dark : light));

            final Drawable warningDrawable = DrawableCompat.wrap(mWarningImage.getDrawable());
            mWarningImage.setImageDrawable(warningDrawable);
            DrawableCompat.setTintList(warningDrawable, (ThemeUtils.DARKTHEME ? dark : light));

            assert mMaterialRippleLayout != null;
            mMaterialRippleLayout.setRippleColor(getResources().getColor(ThemeUtils.DARKTHEME ?
                    R.color.ripple_material_dark : R.color.ripple_material_light));
        }
    }

    /** Apply custom typeface to textviews */
    private void setTypeface() {
        Typeface mediumFont;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediumFont = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } else {
            mediumFont = TypefaceHelper.get(getActivity(), TypefaceHelper.MEDIUM_FONT);
        }

        mWelcomeCardSummary.setTypeface(mediumFont);
        mWelcomeCardFeatures.setTypeface(mediumFont);
        mFeatureCardTitle.setTypeface(mediumFont);
        mAdditionalStatesShow.setTypeface(mediumFont);
        mAdditionalStatesHide.setTypeface(mediumFont);
        mHeaderTotalStateTime.setTypeface(mediumFont);
    }

    /** Animate cardview sliding up from bottom */
    private void setCardAnimation(){
        final Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);

        slideUp.setDuration(500);
        mCardContainer.startAnimation(slideUp);
    }

    /** Animate hiding and showing unused states */
    private void showUnusedStates(boolean enabled) {
        int duration;
        RotateAnimation animRotate;
        final AnimationSet animSet = new AnimationSet(true);

        if (enabled) {
            duration = 300;
            animRotate = new RotateAnimation(0.0f, 180.0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            mAdditionalStatesShow.setVisibility(View.GONE);
            mAdditionalStatesHide.setVisibility(View.VISIBLE);
            mAdditionalStates.setVisibility(View.VISIBLE);
        } else {
            duration = 500;
            animRotate = new RotateAnimation(-180.0f, 0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            mAdditionalStatesShow.setVisibility(View.VISIBLE);
            mAdditionalStatesHide.setVisibility(View.GONE);
            mAdditionalStates.setVisibility(View.GONE);
        }

        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);

        animRotate.setDuration(duration);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);

        mShowImage.startAnimation(animSet);
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
    private void restoreTimers() {
        CpuSpyApp.getCpuStateMonitor().removeOffsets();
        CpuSpyApp.saveOffsets();
        editor.remove("offsets").apply();
    }

    /** Remove view from its parent ViewGroup */
    private void removeView(View v) {
        final ViewGroup mViewGroup = (ViewGroup) v.getParent();
        if (mViewGroup != null) mViewGroup.removeView(v);
    }

    /** Global On click listener for all views */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_welcome:
                this.removeView(mWelcomeCardView);
                editor.putBoolean(WELCOME_SCREEN, false).apply();
                break;
            case R.id.btn_feature:
                this.removeView(mFeatureCardView);
                editor.putBoolean(NEW_FEATURE, false).apply();
                break;
            case R.id.btn_charged:
                editor.putBoolean("autoReset", false).apply();
                refreshData();
                resetTimers();
                break;
            case R.id.card_view_states:
                if (mAdditionalStatesShow.isShown()) {
                    showUnusedStates(true);
                } else {
                    showUnusedStates(false);
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.timer_menu, menu);
        inflater.inflate(R.menu.main_menu, menu);
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
            case R.id.menu_reset:
                resetTimers();
                this.updateView();
                if (!mAdditionalStates.isShown()) showUnusedStates(true);
                SnackbarManager.show(Snackbar.with(getActivity())
                        .text(R.string.snackbar_text_reset)
                        .actionLabel(getResources().getString(R.string.action_dismiss)) // action button label
                        .actionColor(getResources().getColor(R.color.primary)));
                break;
            case R.id.menu_restore:
                restoreTimers();
                this.updateView();
                SnackbarManager.show(Snackbar.with(getActivity())
                        .text(R.string.snackbar_text_restore)
                        .actionLabel(getResources().getString(R.string.action_dismiss)) // action button label
                        .actionColor(getResources().getColor(R.color.primary)));
                break;
            case R.id.menu_settings:
                this.startActivity(new Intent(getActivity(), PrefsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Generate and update all UI elements */
    private void updateView() {
        /** Get the CpuStateMonitor from the app, and iterate over all states,
         * creating a row if the duration is > 0 or otherwise marking it in
         * extraStates (missing) */
        if (mStatesView != null) mStatesView.removeAllViews();
        final List<String> extraStates = new ArrayList<>();
        for (final CpuState state : monitor.getStates()) {
            if (state.duration > 0) {
                final CpuState cpuState = new CpuState(-99, 0);
                final int hiddenPercent = sp.getInt("hidePercent", 0);
                final float percent = (float) state.duration * 100 / monitor.getTotalStateTime();
                if (percent <= hiddenPercent) {
                    cpuState.duration += state.duration;
                    extraStates.add(state.freq / 1000 + "MHz");
                } else {
                    generateStateRow(state, mStatesView);
                }
            } else {
                if (state.freq == 0) {
                    extraStates.add(getResources().getString(R.string.states_deep_sleep));
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

            for (final String s : extraStates) {
                if (n++ > 0)
                    str += ", ";
                str += s;
            }
            mAdditionalStates.setText(str);
        } else {
            mAdditionalStates.setText(R.string.states_empty);
        }
    }

    /** Attempt to update the time-in-state info */
    private void refreshData() {
        this.checkView();
        new RefreshStateDataTask().execute((Void) null);
        this.mSwipeLayout.setRefreshing(false);
    }

    /** @return A nicely formatted String representing tSec seconds */
    private static String sToString(long tSec) {
        final long h = (long)Math.floor(tSec / (60*60));
        final long m = (long)Math.floor((tSec - h*60*60) / 60);
        final long s = tSec % 60;
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
        final LayoutInflater inf = LayoutInflater.from(getActivity());
        final RelativeLayout theRow = (RelativeLayout)inf.inflate(
                R.layout.state_row, parent, false);

        // what percentage we've got
        final float per = (float)state.duration * 100 /
                monitor.getTotalStateTime();
        final String sPer = String.format("%.01f", per) + "%";

        // state name
        String sFreq;
        if (state.freq == 0) {
            sFreq = getResources().getString(R.string.states_deep_sleep);
        } else {
            sFreq = state.freq / 1000 + "MHz";
        }

        // duration
        final long tSec = state.duration / 100;
        final String sDur = sToString(tSec);

        // map UI elements to objects
        final TextView mFreqText = ButterKnife.findById(theRow, R.id.ui_freq_text);
        final TextView mDurText = ButterKnife.findById(theRow, R.id.ui_duration_text);
        final TextView mPerText = ButterKnife.findById(theRow, R.id.ui_percentage_text);
        final ProgressBar mBar = ButterKnife.findById(theRow, R.id.ui_bar);

        // Set UI elements for dark and light themes
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
            try {
                monitor.updateStates();
            } catch (CpuStateMonitorException e) {
                Log.e("CpuSpy", "Problem getting CPU states");
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

    @Override public void onDestroy() {
        super.onDestroy();
        mAutoRefresh = false;
        ButterKnife.unbind(this);
        getActivity().unregisterReceiver(this.mBatInfoReceiver); // unregister receiver
    }
}
