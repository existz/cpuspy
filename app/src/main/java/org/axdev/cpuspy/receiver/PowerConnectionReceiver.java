package org.axdev.cpuspy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.axdev.cpuspy.CpuSpyApp;
import org.axdev.cpuspy.CpuStateMonitor.CpuStateMonitorException;

public class PowerConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CpuSpyApp _app = (CpuSpyApp) context.getApplicationContext();
        String action = intent.getAction();
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percent = (level*100)/scale;

        /** Reset timers if battery is above 97% AND charger is unplugged */
        if(percent >= 97 && action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            try {
                _app.getCpuStateMonitor().setOffsets();
            } catch (CpuStateMonitorException e) {
                // TODO: something
            }
            _app.saveOffsets();
        }
    }
}