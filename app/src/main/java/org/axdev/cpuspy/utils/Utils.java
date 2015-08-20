package org.axdev.cpuspy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;

import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

    /** Check if Xposed is installed or not */
    public static boolean isXposedInstalled(final Context context) {
        try {
            final String XPOSED_INSTALLER_PACKAGE = "de.robv.android.xposed.installer";
            final PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(XPOSED_INSTALLER_PACKAGE, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Check if a service is running or not */
    public static boolean isServiceRunning(final Context context, final Class<?> serviceClass) {
        final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /** Checks if a URL exists or not */
    public static boolean urlExists(final String URLName){
        try {
            //HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            final HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * deep sleep time determined by difference between elapsed (total) boot
     * time and the system uptime (awake)
     */
    public static long getDeepSleep() {
        return (SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()) / 10;
    }

    /**
     * getResources.getColor(int) is now deprecated in Android M. Currently the
     * support library does not offer an alternative.
     */
    public static int getColor(Resources res, int id, Resources.Theme theme)
            throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(id, theme);
        } else {
            return res.getColor(id);
        }
    }
}
