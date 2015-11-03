package com.gio.martino.moonboard;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by Martino on 02/10/2015.
 */
public class MoonboardApplication extends Application {

    MoonboardCommunicationService moonboardComService;

    public MoonboardCommunicationService getMoonboardCommunicationService()
    {
        return moonboardComService;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Intent intent = new Intent(this, MoonboardCommunicationService.class);
        startService(intent);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MoonboardCommunicationService.LocalBinder binder = (MoonboardCommunicationService.LocalBinder)iBinder;
                moonboardComService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }, Context.BIND_AUTO_CREATE);
    }
}
