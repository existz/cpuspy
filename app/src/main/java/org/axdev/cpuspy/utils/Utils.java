package org.axdev.cpuspy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;

public class Utils {

    public static boolean isXposedInstalled(Context context) {
        try {
            final String XPOSED_INSTALLER_PACKAGE = "de.robv.android.xposed.installer";
            final PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(XPOSED_INSTALLER_PACKAGE, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * deep sleep time determined by difference between elapsed (total) boot
     * time and the system uptime (awake)
     */
    public static long getDeepSleep() {
        return (SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()) / 10;
    }
}
