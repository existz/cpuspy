//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.axdev.cpuspy.services.CheckUpdateService;

public class CheckUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());

        if (sp.getBoolean("checkUpdates", true)) {
            final Intent background = new Intent(context, CheckUpdateService.class);
            context.startService(background);
        }
    }
}