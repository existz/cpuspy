package org.axdev.cpuspy.utils;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

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

    /** Check to see if we've clicked outside of a view */
    public static boolean isOutOfBounds(View v, MotionEvent ev) {
        final Rect viewRect = new Rect();
        v.getGlobalVisibleRect(viewRect);
        return !viewRect.contains((int) ev.getRawX(), (int) ev.getRawY());
    }

    public static void setDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }

    public static void openURL(Context c, String s) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(s));
            c.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("CpuSpy", "Error opening: " + s);
        }
    }

    public static int resolveColor(Context context, int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    /** Sets a color filter on a {@link MenuItem} */
    public static void colorMenuItem(final MenuItem menuItem, final int color, final int alpha) {
        final Drawable drawable = menuItem.getIcon();
        if (drawable != null) {
            // If we don't mutate the drawable, then all drawable's with this id will have a color
            // filter applied to it.
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            if (alpha > 0) {
                drawable.setAlpha(alpha);
            }
        }
    }
}
