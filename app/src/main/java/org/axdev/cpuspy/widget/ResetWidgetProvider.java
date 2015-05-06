package org.axdev.cpuspy.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.MainActivity;

public class ResetWidgetProvider extends AppWidgetProvider {

    private static final String RESET_BUTTON = "org.axdev.cpuspy.RESET_TIMERS";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        ComponentName thisWidget = new ComponentName(context, ResetWidgetProvider.class);

        int[] allWidgetInstancesIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetInstancesIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_reset_layout);

            Intent intent = new Intent(context, ResetWidgetProvider.class);
            intent.setAction(RESET_BUTTON);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.widget_reset_img, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(RESET_BUTTON)) {
            try {
                MainActivity.resetTimers();
                Toast.makeText(context, R.string.widget_reset_success, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, R.string.widget_reset_error, Toast.LENGTH_SHORT).show();
            }
        }

        super.onReceive(context, intent);
    }
}