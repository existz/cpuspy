package org.axdev.cpuspy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

import org.axdev.cpuspy.CpuSpyApp;

public class PowerConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : 0;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : 0;
        int percent = (level*100)/scale;

        final SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());

        if (sp.getBoolean("autoReset", true)) {
            /** Reset timers if battery is above 97% AND charger is unplugged */
            if (percent >= 97 && action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                CpuSpyApp.resetTimers();
            }
        }
    }
}