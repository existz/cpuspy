//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.ui;

// imports

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import org.axdev.cpuspy.CpuSpyApp;
import org.axdev.cpuspy.CpuStateMonitor;
import org.axdev.cpuspy.CpuStateMonitor.CpuState;
import org.axdev.cpuspy.CpuStateMonitor.CpuStateMonitorException;
import org.axdev.cpuspy.R;

import java.util.ArrayList;
import java.util.List;

/** main activity class */
public class HomeActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "CpuSpy";
    private static final String VERSION_KEY = "version_number";
    private static final String WELCOME_SCREEN = "welcomeScreenShown";

    private CpuSpyApp _app = null;
    private SwipeRefreshLayout swipeLayout;

    // the views
    private LinearLayout    _uiStatesView = null;
    private TextView        _uiChargedView = null;
    private ImageView       _uiChargedImg = null;
    private TextView        _uiAdditionalStates = null;
    private TextView        _uiTotalStateTime = null;
    private TextView        _uiHeaderAdditionalStates = null;
    private TextView        _uiHeaderTotalStateTime = null;
    private TextView        _uiHeaderKernelString = null;
    private TextView        _uiStatesWarning = null;
    private TextView        _uiKernelString = null;

    /** whether or not we're updating the data in the background */
    private boolean     _updatingData = false;

    /** whether or not auto refresh is enabled */
    private boolean     mAutoRefresh = false;

    /** lets us know if the battery is fully charged or not */
    private boolean     mIsCharged = false;

    /** Initialize the Activity */
    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        // second argument is the default to use if the preference can't be found
        boolean welcomeScreenShown = sp.getBoolean(WELCOME_SCREEN, false);

        if (!welcomeScreenShown) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }

        // inflate the view, stash the app context, and get all UI elements
        setContentView(R.layout.home_layout);
        _app = (CpuSpyApp)getApplicationContext();
        checkVersion();
        findViews();

        // see if we're updating data during a config change (rotate screen)
        if (savedInstanceState != null) {
            _updatingData = savedInstanceState.getBoolean("updatingData");
        }

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.primary,
                R.color.accent);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Register receiver
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    /** When the activity is about to change orientation */
    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("updatingData", _updatingData);
    }

    @Override public void onStart () {
        super.onStart();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (sp.getBoolean("autoRefresh", true)) {
            mAutoRefresh = true;
            mHandler.post(refreshAuto);
        } else {
            mAutoRefresh = false;
        }
    }

    /** Disable auto refresh when app is in the background */
    @Override public void onPause () {
        super.onPause();
        mAutoRefresh = false;
    }

    /** Update the view when the application regains focus */
    @Override public void onResume () {
        super.onResume();
        refreshData();
    }

    @Override public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
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

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            mIsCharged = percent >= 97 && isCharging;

            if (sp.getBoolean("autoReset", true) && mIsCharged) {
                updateView();
            }
        }
    };

    /** Show WhatsNewDialog if versionCode has changed */
    private void checkVersion() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int currentVersionNumber = 0;
        int savedVersionNumber = sp.getInt(VERSION_KEY, 0);
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception ignored) {}

        if (currentVersionNumber > savedVersionNumber) {
            showWhatsNewDialog();
            Editor editor = sp.edit();
            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }

    private void showWhatsNewDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.changelog_dialog_title)
                .customView(R.layout.dialog_webview, false)
                .negativeText(R.string.action_dismiss)
                .build();
        WebView webView = (WebView) dialog.getCustomView().findViewById(R.id.webview);
        webView.loadUrl("file:///android_asset/webview.html");
        dialog.show();
    }

    /** Map all of the UI elements to member variables */
    void findViews() {
        _uiStatesView = (LinearLayout)findViewById(R.id.ui_states_view);
        _uiChargedImg = (ImageView)findViewById(R.id.ui_charged_img);
        _uiChargedView = (TextView)findViewById(R.id.ui_charged_view);
        _uiKernelString = (TextView)findViewById(R.id.ui_kernel_string);
        _uiHeaderKernelString = (TextView) findViewById(
                R.id.ui_header_kernel_string);
        _uiAdditionalStates = (TextView)findViewById(
                R.id.ui_additional_states);
        _uiHeaderAdditionalStates = (TextView)findViewById(
                R.id.ui_header_additional_states);
        _uiHeaderTotalStateTime = (TextView)findViewById(
                R.id.ui_header_total_state_time);
        _uiStatesWarning = (TextView)findViewById(R.id.ui_states_warning);
        _uiTotalStateTime = (TextView)findViewById(R.id.ui_total_state_time);
    }

    /** called when we want to infalte the menu */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // request inflater from activity and inflate into its menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        inflater.inflate(R.menu.settings_menu, menu);

        // made it
        return true;
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // what it do mayne
        switch (item.getItemId()) {
        /* pressed the load menu button */
        case R.id.menu_reset:
            try {
                _app.getCpuStateMonitor().setOffsets();
            } catch (CpuStateMonitorException e) {
                // TODO: something
            }
            _app.saveOffsets();
            updateView();
            SnackbarManager.show(Snackbar.with(this)
                .text(R.string.snackbar_text_reset) // text to display
                .actionLabel(R.string.action_dismiss) // action button label
                .actionColor(Color.parseColor("#f4b400")));
            break;
        case R.id.menu_restore:
            _app.getCpuStateMonitor().removeOffsets();
            _app.saveOffsets();
            updateView();
            SnackbarManager.show(Snackbar.with(this)
                .text(R.string.snackbar_text_restore) // text to display
                .actionLabel(R.string.action_dismiss) // action button label
                .actionColor(Color.parseColor("#f4b400")));
            break;
        case R.id.menu_settings:
            Intent intent = new Intent(this, PrefsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Generate and update all UI elements */
    void updateView() {
        /** Get the CpuStateMonitor from the app, and iterate over all states,
         * creating a row if the duration is > 0 or otherwise marking it in
         * extraStates (missing) */

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Medium.ttf");

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        CpuStateMonitor monitor = _app.getCpuStateMonitor();
        _uiStatesView.removeAllViews();
        List<String> extraStates = new ArrayList<>();
        for (CpuState state : monitor.getStates()) {
            if (state.duration > 0) {
                generateStateRow(state, _uiStatesView);
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
        _uiTotalStateTime.setText(sToString(totTime));
        _uiHeaderTotalStateTime.setTypeface(tf);

        // for all the 0 duration states, add the the Unused State area
        if (extraStates.size() > 0) {
            int n = 0;
            String str = "";

            for (String s : extraStates) {
                if (n++ > 0)
                    str += ", ";
                str += s;
            }

            _uiAdditionalStates.setVisibility(View.VISIBLE);
            _uiHeaderAdditionalStates.setVisibility(View.VISIBLE);
            _uiHeaderAdditionalStates.setTypeface(tf);
            _uiAdditionalStates.setText(str);
        } else {
            _uiAdditionalStates.setVisibility(View.GONE);
            _uiHeaderAdditionalStates.setVisibility(View.GONE);
        }

        // kernel line
        _uiHeaderKernelString.setTypeface(tf);
        _uiKernelString.setText(_app.getKernelVersion());

        /** Reset timers and show info when battery is charged */
        if (sp.getBoolean("autoReset", true) && mIsCharged) {
            _uiStatesWarning.setVisibility(View.GONE);
            _uiStatesView.setVisibility(View.GONE);
            _uiHeaderTotalStateTime.setVisibility(View.GONE);
            _uiTotalStateTime.setVisibility(View.GONE);
            _uiHeaderAdditionalStates.setVisibility(View.GONE);
            _uiAdditionalStates.setVisibility(View.GONE);
            _uiHeaderKernelString.setVisibility(View.GONE);
            _uiKernelString.setVisibility(View.GONE);
            _uiChargedView.setVisibility(View.VISIBLE);
            _uiChargedImg.setVisibility(View.VISIBLE);

            _uiChargedView.setTypeface(tf);

            try {
                _app.getCpuStateMonitor().setOffsets();
            } catch (CpuStateMonitorException e) {
                // TODO: something
            }
            _app.saveOffsets();
        } else {
            _uiStatesWarning.setVisibility(View.GONE);
            _uiChargedView.setVisibility(View.GONE);
            _uiChargedImg.setVisibility(View.GONE);
            _uiStatesView.setVisibility(View.VISIBLE);
            _uiHeaderTotalStateTime.setVisibility(View.VISIBLE);
            _uiTotalStateTime.setVisibility(View.VISIBLE);
            _uiHeaderKernelString.setVisibility(View.VISIBLE);
            _uiKernelString.setVisibility(View.VISIBLE);
        }

        /** show the red warning label if no states found */
        if (monitor.getStates().size() == 0) {
            _uiStatesWarning.setVisibility(View.VISIBLE);
            _uiHeaderKernelString.setVisibility(View.VISIBLE);
            _uiKernelString.setVisibility(View.VISIBLE);
            _uiHeaderTotalStateTime.setVisibility(View.GONE);
            _uiTotalStateTime.setVisibility(View.GONE);
            _uiStatesView.setVisibility(View.GONE);
            _uiChargedView.setVisibility(View.GONE);
            _uiChargedImg.setVisibility(View.GONE);

            _uiStatesWarning.setTypeface(tf);

            return; // let's end this
        }
    }

    /** Attempt to update the time-in-state info */
    void refreshData() {
        if (!_updatingData) {
            new RefreshStateDataTask().execute((Void)null);
        }
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
        LayoutInflater inf = LayoutInflater.from(_app);
        LinearLayout theRow = (LinearLayout)inf.inflate(
                R.layout.state_row, parent, false);

        // what percetnage we've got
        CpuStateMonitor monitor = _app.getCpuStateMonitor();
        float per = (float)state.duration * 100 /
            monitor.getTotalStateTime();
        String sPer = (int)per + "%";

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
        TextView freqText = (TextView)theRow.findViewById(R.id.ui_freq_text);
        TextView durText = (TextView)theRow.findViewById(
                R.id.ui_duration_text);
        TextView perText = (TextView)theRow.findViewById(
                R.id.ui_percentage_text);
        ProgressBar bar = (ProgressBar)theRow.findViewById(R.id.ui_bar);

        // modify the row
        freqText.setText(sFreq);
        perText.setText(sPer);
        durText.setText(sDur);
        bar.setProgress((int)per);

        // add it to parent and return
        parent.addView(theRow);
        return theRow;
    }

    /** Keep updating the state data off the UI thread for slow devices */
    private class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {

        /** Stuff to do on a seperate thread */
        @Override protected Void doInBackground(Void... v) {
            CpuStateMonitor monitor = _app.getCpuStateMonitor();
            try {
                monitor.updateStates();
            } catch (CpuStateMonitorException e) {
                Log.e(TAG, "Problem getting CPU states");
            }

            return null;
        }

        /** Executed on the UI thread right before starting the task */
        @Override protected void onPreExecute() {
            _updatingData = true;
        }

        /** Executed on UI thread after task */
        @Override protected void onPostExecute(Void v) {
            _updatingData = false;
            updateView();
        }
    }

    /** Update data every 1s if auto refresh is enabled */
    private final Handler mHandler = new Handler();

    private final Runnable refreshAuto = new Runnable() {
        public void run() {
            if(mAutoRefresh) {
                refreshData();
                mHandler.postDelayed(refreshAuto, 1000); // 1 second
            }
        }
    };

    @Override protected void onDestroy() {
        mAutoRefresh = false;
        this.unregisterReceiver(this.mBatInfoReceiver); // unregister receiver
        super.onDestroy();
    }
}
