//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.Display;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.MainActivity;
import org.axdev.cpuspy.activity.ThemedActivity;
import org.axdev.cpuspy.utils.Utils;

import java.util.concurrent.TimeUnit;

public class SleepService extends Service {

    private boolean isRunning;
    private int notificationID;
    private long lastDeepSleep;
    private BroadcastReceiver NotificationButtonReceiver;
    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;
    private Thread backgroundThread;

    private final String buttonReceiver = "disable-button-receiver";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.mContext = getApplicationContext();
        this.isRunning = false;
        this.backgroundThread = new Thread(monitorDeepSleep);
        this.lastDeepSleep = Utils.getDeepSleep();

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        this.NotificationButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

                if (mNotifyManager != null) {
                    mNotifyManager.cancel(notificationID);
                    sp.edit().putBoolean("sleepDetection", false).apply();
                    stopSelf();
                }
            }
        };
        // Registers the receiver
        this.registerReceiver(this.NotificationButtonReceiver,
                new IntentFilter(buttonReceiver));
    }

    /** Determine if deep sleep increases when screen is off */
    private Runnable monitorDeepSleep = new Runnable() {
        public void run() {
            while (isRunning) {
                try {
                    final Resources res = getResources();

                    if (!isScreenOn(mContext)
                            && !isPluggedIn(mContext)
                            && !isUserInCall(mContext)
                            && !isMusicPlaying(mContext)) {

                        // Wait 10 minutes before checking deep sleep
                        TimeUnit.MINUTES.sleep(10);

                        notificationID = 1;
                        final long currentDeepSleep = Utils.getDeepSleep();

                        if (currentDeepSleep <= lastDeepSleep) {
                            // PendingIntent to launch app when notification is clicked
                            final Intent mainIntent = new Intent(mContext, MainActivity.class);
                            final PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                                    0, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                            final int color = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("primary_color", 0);
                            final int primaryColor = color == 0 ? ContextCompat.getColor(mContext, R.color.primary) : color;

                            mBuilder = new NotificationCompat.Builder(mContext)
                                    .setContentIntent(contentIntent)
                                    .setContentTitle(res.getString(R.string.notification_warning))
                                    .setContentText(res.getString(R.string.notification_deep_sleep))
                                    .setColor(primaryColor)
                                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                    .setSmallIcon(R.drawable.ic_notify_deepsleep)
                                    .setOnlyAlertOnce(true);

                            // Add PendingIntent for disable button
                            final Intent buttonIntent = new Intent(buttonReceiver);
                            final PendingIntent btPendingIntent = PendingIntent.getBroadcast(mContext, 0, buttonIntent, 0);

                            // Add "X" icon to disable button for KitKat devices only
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mBuilder.addAction(0, res.getString(R.string.notification_disable), btPendingIntent);
                            } else {
                                mBuilder.addAction(R.drawable.ic_notify_close, res.getString(R.string.notification_disable), btPendingIntent);
                            }

                            // Send the notification
                            mNotifyManager.notify(notificationID, mBuilder.build());
                        } else {
                            lastDeepSleep = currentDeepSleep;
                            if (mBuilder != null) mNotifyManager.cancel(notificationID);
                        }
                    }
                    Thread.sleep(300000); // 5 minutes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /** @return true if music is playing */
    private boolean isMusicPlaying(final Context context) {
        final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.isMusicActive();
    }

    /** @return true if connected to AC/USB charger */
    private boolean isPluggedIn(final Context context) {
        final Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        final int plugged = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : 0;
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    /** @return true if device call state: off-hook (in call) */
    private boolean isUserInCall(final Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
    }

    /**
     * Is the screen of the device on.
     * @param context the context
     * @return true when (at least one) screen is on
     */
    private boolean isScreenOn(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (final Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

    @Override
    public void onDestroy() {
        this.isRunning = false;
        this.unregisterReceiver(this.NotificationButtonReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }
}