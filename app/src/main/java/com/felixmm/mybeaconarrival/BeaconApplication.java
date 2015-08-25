package com.felixmm.mybeaconarrival;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class BeaconApplication extends Application implements BootstrapNotifier {

    RegionBootstrap regionBootstrap;
    BackgroundPowerSaver backgroundPowerSaver;

    String found_msg = "Your luggage is nearby now.";
    String lost_msg = "Signal lost. Please wait.";

    public void startBeaconMonitoring() {
        if (regionBootstrap == null) {
            String myBeacon = SharedPreferenceHelper.getSharedStringPref(this, "myBeacon", "");
            Region region = new Region("backgroundRegion",
                    Identifier.parse(myBeacon), null, null);
            regionBootstrap = new RegionBootstrap(this, region);
        }
    }

    public void stopBeaconMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
        }
    }

    public void onCreate() {
        super.onCreate();

        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        if (SharedPreferenceHelper.getSharedBooleanPref(this, "scanning", false))
            startBeaconMonitoring();

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager.setBackgroundBetweenScanPeriod(5000);
    }

    @Override
    public void didEnterRegion(Region region) {
        sendNotification(found_msg);
    }

    @Override
    public void didExitRegion(Region region) {
        sendNotification(lost_msg);
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        // Ignore
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String message) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher);

        if (message.equals(found_msg))
            builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000 });

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}
