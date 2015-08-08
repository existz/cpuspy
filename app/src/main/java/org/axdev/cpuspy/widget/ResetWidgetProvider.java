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
import org.axdev.cpuspy.fragments.TimerFragment;

public class ResetWidgetProvider extends AppWidgetProvider {

    private static final String RESET_BUTTON = "org.axdev.cpuspy.RESET_TIMERS";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final ComponentName thisWidget = new ComponentName(context, ResetWidgetProvider.class);

        final int[] allWidgetInstancesIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (final int widgetId : allWidgetInstancesIds) {

            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_reset_layout);

            final Intent intent = new Intent(context, ResetWidgetProvider.class);
            intent.setAction(RESET_BUTTON);

            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
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
                TimerFragment.resetTimers();
                Toast.makeText(context, R.string.widget_reset_success, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, R.string.widget_reset_error, Toast.LENGTH_SHORT).show();
            }
        }

        super.onReceive(context, intent);
    }
}