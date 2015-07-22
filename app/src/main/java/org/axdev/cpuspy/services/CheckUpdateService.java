//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import org.axdev.cpuspy.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class CheckUpdateService extends Service {

    private boolean isRunning;
    private Thread backgroundThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.isRunning = false;
        this.backgroundThread = new Thread(checkForUpdate);
    }

    /** Determine if deep sleep increases when screen is off */
    private Runnable checkForUpdate = new Runnable() {
        public void run() {
            while (isRunning) {
                try {
                    int notificationID = 2;
                    final String upcomingVersion = getResources().getString(R.string.notification_update_summary);
                    final String upcomingVersionURL = "https://app.box.com/cpuspy-v315";

                    if (urlExists(upcomingVersionURL)) {
                        // Intent to launch XDA post when notification is clicked
                        final Intent mainIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://goo.gl/AusQy8"));
                        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                        // Build the notification
                        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                .setContentIntent(contentIntent)
                                .setContentTitle(getResources().getString(R.string.notification_update_title))
                                .setContentText(upcomingVersion)
                                .setColor(getResources().getColor(R.color.primary))
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setSmallIcon(R.drawable.ic_notify_update)
                                .setOnlyAlertOnce(true);

                        // Send the notification
                        final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotifyManager.notify(notificationID, mBuilder.build());
                    }

                    // Set the time when last checked for update
                    final String timestamp = DateFormat.format("h:mm a", new Date()).toString();
                    final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    sp.edit().putString("lastChecked", timestamp).apply();

                    Thread.sleep(28800000); // 8hrs
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean urlExists(String URLName){
        try {
            //HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroy() {
        this.isRunning = false;
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