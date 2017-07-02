package org.lamorim.huxflooderapp.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by lucas on 24/12/2016.
 */

public class ReceiverNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("checkbox_notification", false))
        TaskBroadcastReceiver.setupAlarm(context);
    }
}
