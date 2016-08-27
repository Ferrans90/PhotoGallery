package com.ferran.photogallery.Presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        if (Build.VERSION.SDK_INT <= 20) {
            PollService.setServiceAlarm(context, isOn);
        } else {
            PollService2.setService(context, isOn);
        }


    }
}
