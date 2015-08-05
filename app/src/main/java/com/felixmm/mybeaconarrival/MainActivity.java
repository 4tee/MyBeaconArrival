package com.felixmm.mybeaconarrival;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private TextView noticeTxt;
    private ImageView imgPos;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noticeTxt = (TextView) findViewById(R.id.notice_txt);
        imgPos = (ImageView) findViewById(R.id.beacon_position);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        verifyBluetooth();

        beaconManager.bind(this);
    }



    private void showStatus(final String line) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noticeTxt.setText(line);
                if (line.equals("immediate")) imgPos.setImageResource(R.drawable.immediate);
                else if (line.equals("near")) imgPos.setImageResource(R.drawable.near);
                else if (line.equals("far")) imgPos.setImageResource(R.drawable.far);
                else imgPos.setImageResource(R.drawable.background);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    //https://github.com/YadaPublic/beacon/blob/master/Billboard/src/org/altbeacon/beacon/Beacon.java
    protected static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine distance, return -1.
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
//            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
//            return accuracy;
            return (0.42093)*Math.pow(ratio,6.9476) + 0.54992;
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                String myBeacon = SharedPreferenceHelper.getSharedStringPref(MainActivity.this, "myBeacon", "");

                if (beacons.size() <= 0) {
                    showStatus("Searching beacon signal..");
                }
                else {
                    boolean foundBeacon = false;

                    for (Beacon beacon : beacons) {
                        String UUID = beacon.getId1().toString();

                        if (myBeacon.toLowerCase().equals(UUID.toLowerCase())) {
                            foundBeacon = true;
                            double calDist = calculateDistance(beacon.getTxPower(), beacon.getRssi());
                            Log.d("ts","calDist:" +calDist+" VS dist:" + beacon.getDistance());

                            if (calDist < 0.5) {
                                // This beacon is immediate (< 0.5 meters)
                                showStatus("immediate");
                            } else if (calDist < 3.0) {
                                // This beacon is near (0.5 to 3 meters)
                                showStatus("near");
                            } else {
                                // This beacon is far (> 3 meters)
                                showStatus("far");
                            }
                        }
                    }

                    if (!foundBeacon) showStatus("Your beacon is not detected yet.");

                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addBeacon:
                Intent settingIntent = new Intent(this, BeaconSettings.class);
                startActivity(settingIntent);
                return true;
            default:
                return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void verifyBluetooth() {

        try {
            if (!beaconManager.checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            noticeTxt.setText("This device does not support bluetooth.");
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                noticeTxt.setText(getString(R.string.enable_bluetooth));
            } else {
                noticeTxt.setText("No beacon is detected yet.");
            }
        }
    }
}
