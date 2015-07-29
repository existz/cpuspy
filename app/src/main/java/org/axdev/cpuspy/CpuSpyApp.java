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
import android.util.SparseArray;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/** main application class */
public class CpuSpyApp extends Application {

    public static final String PREF_OFFSETS = "offsets";
    private static SharedPreferences sp;

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
        String prefs = sp.getString(PREF_OFFSETS, "");

        if (prefs.length() < 1) return;

        // split the string by peroids and then the info by commas and load
        final SparseArray<Long> offsets = new SparseArray<>();
        final String[] sOffsets = prefs.split(",");
        for (String offset : sOffsets) {
            final String[] parts = offset.split(" ");
            offsets.put (Integer.parseInt(parts[0]),
                    Long.valueOf(parts[1]));
        }

        _monitor.setOffsets(offsets);
    }

    /**
     * Save the state-time offsets as a string
     * e.g. "100 24, 200 251, 500 124 etc
     */
    public static void saveOffsets() {
        // build the string by iterating over the freq->duration map
        String str = "";
        final SparseArray<Long> offsets = _monitor.getOffsets();
        int count = offsets.size();
        for (int i = 0; i < count; i++) {
            str += offsets.keyAt(i) + " " + offsets.valueAt(i) + ",";
        }

        final Editor editor = sp.edit();
        editor.putString(PREF_OFFSETS, str).apply();
    }
}
