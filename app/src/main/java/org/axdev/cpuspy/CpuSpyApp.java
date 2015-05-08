//-----------------------------------------------------------------------------
//
// (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy;

// imports

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import com.squareup.leakcanary.LeakCanary;

import org.axdev.cpuspy.utils.CPUUtils;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/** main application class */
public class CpuSpyApp extends Application {

    public static final String PREF_OFFSETS = "offsets";
    public static SharedPreferences sp;

    /** the long-living object used to monitor the system frequency states */
    public static final CpuStateMonitor _monitor = new CpuStateMonitor();

    /**
     * On application start, load the saved offsets and stash the
     * current kernel version string
     */
    @Override public void onCreate() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize and start automatic crash reporting
        if(sp.getBoolean("crashReport", true)) {
            Fabric.with(this, new Crashlytics());
        }

        loadOffsets();
        CPUUtils.getKernelVersion();
        LeakCanary.install(this);
    }

    /** @return the internal CpuStateMonitor object */
    public static CpuStateMonitor getCpuStateMonitor() {
        return _monitor;
    }

    /**
     * Load the saved string of offsets from preferences and put it into
     * the state monitor
     */
    private void loadOffsets() {
        String prefs = sp.getString (PREF_OFFSETS, "");

        if (prefs == null || prefs.length() < 1) {
            return;
        }

        // split the string by peroids and then the info by commas and load
        Map<Integer, Long> offsets = new HashMap<>();
        String[] sOffsets = prefs.split(",");
        for (String offset : sOffsets) {
            String[] parts = offset.split(" ");
            offsets.put (Integer.parseInt(parts[0]),
                    Long.parseLong(parts[1]));
        }

        _monitor.setOffsets(offsets);
    }

    /**
     * Save the state-time offsets as a string
     * e.g. "100 24, 200 251, 500 124 etc
     */
    public static void saveOffsets() {
        final Editor editor = sp.edit();

        // build the string by iterating over the freq->duration map
        String str = "";
        for (Map.Entry<Integer, Long> entry :
                _monitor.getOffsets().entrySet()) {
            str += entry.getKey() + " " + entry.getValue() + ",";
        }

        editor.putString(PREF_OFFSETS, str);
        editor.commit();
    }
}
