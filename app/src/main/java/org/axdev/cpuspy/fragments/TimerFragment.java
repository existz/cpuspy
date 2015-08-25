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
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import org.axdev.cpuspy.CpuSpyApp;
import org.axdev.cpuspy.CpuState;
import org.axdev.cpuspy.CpuStateMonitor;
import org.axdev.cpuspy.CpuStateMonitor.CpuStateMonitorException;
import org.axdev.cpuspy.activity.PrefsActivity;
import org.axdev.cpuspy.animation.ProgressBarAnimation;
import org.axdev.cpuspy.listeners.ShakeEventListener;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

/** main activity class */
public class TimerFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    // main ui views
    @Bind(R.id.card_view_states) CardView mStatesCardView;
    @Bind(R.id.card_view_welcome) CardView mWelcomeCardView;
    @Bind(R.id.card_view_feature) CardView mFeatureCardView;
    @Bind(R.id.card_view_time) CardView mTimeCardView;
    @Bind(R.id.img_show) ImageView mShowImage;
    @Bind(R.id.card_container) LinearLayout mCardContainer;
    @Bind(R.id.ui_states_view) LinearLayout mStatesView;
    @Bind(R.id.ui_charged_view) LinearLayout mChargedView;
    @Bind(R.id.ui_states_warning) LinearLayout mStatesWarning;
    @Bind(R.id.swipe_container) SwipeRefreshLayout mSwipeLayout;
    @Bind(R.id.ui_additional_states) TextView mAdditionalStates;
    @Bind(R.id.ui_additional_states_count) TextView mAdditionalStatesCount;
    @Bind(R.id.ui_total_state_time) TextView mTotalStateTime;
    @Bind(R.id.ui_header_total_state_time) TextView mHeaderTotalStateTime;
    @Bind(R.id.welcome_summary) TextView mWelcomeCardSummary;
    @Bind(R.id.welcome_features) TextView mWelcomeCardFeatures;
    @Bind(R.id.feature_title) TextView mFeatureCardTitle;

    private final String WELCOME_SCREEN = "welcomeScreenShown";
    private final String NEW_FEATURE = "newFeatureShown";

    private CpuStateMonitor monitor;
    private Handler mHandler;
    private Resources res;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    private SharedPreferences sp;
    private Typeface robotoMedium;

    private boolean mAutoRefresh;
    private boolean mIsAnimating;
    private boolean mIsCharged;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.timer_layout, container, false);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mHandler = new Handler();
        res = getResources();
        mIsAnimating = true;
        monitor = CpuSpyApp.getCpuStateMonitor();
        robotoMedium = TypefaceHelper.mediumTypeface(getActivity());
        this.checkView();

        /** Apply Roboto-Medium typeface to textviews */
        mWelcomeCardSummary.setTypeface(robotoMedium);
        mWelcomeCardFeatures.setTypeface(robotoMedium);
        mFeatureCardTitle.setTypeface(robotoMedium);
        mAdditionalStatesCount.setTypeface(robotoMedium);
        mHeaderTotalStateTime.setTypeface(robotoMedium);

        /** Show WhatsNewDialog if versionCode has changed */
        int currentVersionNumber = 0;
        int savedVersionNumber = sp.getInt("version_number", 0);
        try {
            final PackageInfo pi = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception ignored) {}

        if (currentVersionNumber > savedVersionNumber) {
            final WhatsNewDialog newFragment = new WhatsNewDialog();
            newFragment.show(getActivity().getFragmentManager(), "whatsnew");
            sp.edit().putInt("version_number", currentVersionNumber).apply();
        }

        /** Remove welcome cardview if its already been shown */
        boolean welcomeScreenShown = sp.getBoolean(WELCOME_SCREEN, true);
        if (!welcomeScreenShown) { this.removeView(mWelcomeCardView); }

        /** Remove new feature cardview if its already been shown */
        boolean newFeatureShown = sp.getBoolean(NEW_FEATURE, true);
        if (!newFeatureShown) { this.removeView(mFeatureCardView); }

        /** Set colors and listener for SwipeRefreshLayout */
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), android.R.color.white));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getActivity(), R.color.primary));

        /** Add listener for shake to refresh */
        if (!mAutoRefresh) {
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mSensorListener = new ShakeEventListener();
                mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
                    @Override
                    public void onShake() {
                        if (!mSwipeLayout.isRefreshing()) refreshData();
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

            sp = PreferenceManager.getDefaultSharedPreferences(context);

            mIsCharged = percent >= 97 && isCharging;
            if (sp.getBoolean("autoReset", true) && !mAutoRefresh) checkView();
        }
    };

    private void checkView() {
        final File timeInState = new File("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
        final boolean mStatesNotFound = !timeInState.exists() || timeInState.length() == 0;

        // Reset timers and show info when battery is charged
        if (sp.getBoolean("autoReset", true) && mIsCharged) {
            // Disable layout transitions
            mCardContainer.setLayoutTransition(null);

            mStatesWarning.setVisibility(View.GONE);
            mStatesCardView.setVisibility(View.GONE);
            mTimeCardView.setVisibility(View.GONE);
            mWelcomeCardView.setVisibility(View.GONE);
            mChargedView.setVisibility(View.VISIBLE);

            CpuSpyApp.resetTimers();
        } else {
            mStatesWarning.setVisibility(View.GONE);
            mChargedView.setVisibility(View.GONE);
            mStatesCardView.setVisibility(View.VISIBLE);
            mTimeCardView.setVisibility(View.VISIBLE);
        }

        // show warning label if no states found
        if (mStatesNotFound) {
            // Disable layout transitions
            mCardContainer.setLayoutTransition(null);

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

    /** Animate hiding and showing unused states */
    private void showUnusedStates(boolean enabled) {
        int duration;
        RotateAnimation animRotate;
        final AnimationSet animSet = new AnimationSet(true);

        if (enabled) {
            duration = res.getInteger(R.integer.animationShort);
            animRotate = new RotateAnimation(0.0f, -180.0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            mAdditionalStates.setVisibility(View.VISIBLE);
        } else {
            duration = res.getInteger(R.integer.animationMedium);
            animRotate = new RotateAnimation(-180.0f, 0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

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

    /** Remove view from its parent ViewGroup */
    private void removeView(View v) {
        final ViewGroup mViewGroup = (ViewGroup) v.getParent();
        if (mViewGroup != null) mViewGroup.removeView(v);
    }

    /** Bind button listeners */
    @OnClick(R.id.btn_welcome)
    void welcomeButton() {
        this.removeView(mWelcomeCardView);
        sp.edit().putBoolean(WELCOME_SCREEN, false).apply();
    }

    @OnClick(R.id.btn_feature)
    void featureButton() {
        this.removeView(mFeatureCardView);
        sp.edit().putBoolean(NEW_FEATURE, false).apply();
    }

    @OnClick(R.id.btn_charged)
    void chargedButton() {
        sp.edit().putBoolean("autoReset", false).apply();
        refreshData();
        CpuSpyApp.resetTimers();
    }

    @OnClick(R.id.card_view_states)
    void unusedStatesButton() {
        if (!mAdditionalStates.isShown()) {
            showUnusedStates(true);
        } else {
            showUnusedStates(false);
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
                CpuSpyApp.resetTimers();
                this.updateView();
                if (!mAdditionalStates.isShown()) showUnusedStates(true);
                SnackbarManager.show(Snackbar.with(getActivity())
                        .text(res.getString(R.string.snackbar_text_reset))
                        .actionLabel(res.getString(R.string.action_dismiss)) // action button label
                        .actionLabelTypeface(robotoMedium)
                        .actionColor(ContextCompat.getColor(getActivity(), R.color.primary)));
                break;
            case R.id.menu_restore:
                CpuSpyApp.restoreTimers();
                sp.edit().remove("offsets").apply();
                this.updateView();
                SnackbarManager.show(Snackbar.with(getActivity())
                        .text(res.getString(R.string.snackbar_text_restore))
                        .actionLabelTypeface(robotoMedium)
                        .actionLabel(res.getString(R.string.action_dismiss)) // action button label
                        .actionColor(ContextCompat.getColor(getActivity(), R.color.primary)));
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
        final List<String> extraStates = new ArrayList<>(monitor.getStates().size());
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
                    extraStates.add(res.getString(R.string.states_deep_sleep));
                } else {
                    extraStates.add(state.freq / 1000 + "MHz");
                }
            }
        }

        // get the total number of unused states
        final int count = extraStates.size();
        mAdditionalStatesCount.setText(String.valueOf(count) + res.getString(R.string.unused_states_count));

        // update the total state time
        final long totTime = monitor.getTotalStateTime() / 100;
        mTotalStateTime.setText(sToString(totTime));

        // for all the 0 duration states, add the the Unused State area
        if (extraStates.size() > 0) {
            int n = 0;
            final StringBuilder stringBuilder = new StringBuilder();

            for (final String s : extraStates) {
                if (n++ > 0) stringBuilder.append(",").append(" ");
                stringBuilder.append(s);
            }

            mAdditionalStates.setText(stringBuilder.toString());
        } else {
            mAdditionalStates.setText(res.getString(R.string.states_empty));
        }
    }

    /** Attempt to update the time-in-state info */
    private void refreshData() {
        new RefreshStateDataTask().execute();
        if (mSwipeLayout != null) mSwipeLayout.setRefreshing(false);
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
    private void generateStateRow(CpuState state, ViewGroup parent) {
        // inflate the XML into a view in the parent
        final RelativeLayout theRow = (RelativeLayout) getActivity().getLayoutInflater().inflate(
                R.layout.state_row, parent, false);

        // what percentage we've got
        final float per = (float)state.duration * 100 /
                monitor.getTotalStateTime();
        final String sPer = String.format("%.01f", per) + "%";

        final String sFreq = state.freq == 0 ? res.getString(R.string.states_deep_sleep) : state.freq / 1000 + "MHz";

        // duration
        final long tSec = state.duration / 100;
        final String sDur = sToString(tSec);

        // map UI elements to objects
        final TextView mFreqText = ButterKnife.findById(theRow, R.id.ui_freq_text);
        final TextView mDurText = ButterKnife.findById(theRow, R.id.ui_duration_text);
        final TextView mPerText = ButterKnife.findById(theRow, R.id.ui_percentage_text);
        final ProgressBar mBar = ButterKnife.findById(theRow, R.id.ui_bar);

        // modify the row
        mFreqText.setText(sFreq);
        mPerText.setText(sPer);
        mDurText.setText(sDur);

        // animate progress bar
        if (mIsAnimating) {
            final Runnable progressAnimation = new Runnable() {
                @Override
                public void run() {
                    final ProgressBarAnimation anim = new ProgressBarAnimation(mBar, 1000);
                    mBar.setMax(100 * 100);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.setProgress((Math.round(per)) * 100);
                    anim.setDuration(res.getInteger(R.integer.progressAnimationDuration));
                    mBar.startAnimation(anim);
                    mIsAnimating = false;
                }
            };
            mHandler.post(progressAnimation);
        } else {
            mBar.setMax(100);
            mBar.setProgress((Math.round(per)));
        }

        // add it to parent and return
        parent.addView(theRow);
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
            if (getActivity() != null) {
                updateView();
                checkView();
            }
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
