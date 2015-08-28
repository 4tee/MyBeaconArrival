package com.felixmm.mybeaconarrival;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class BeaconRegistration extends AppCompatActivity {

    static final int GET_QR_VALUE = 1;

    TextView QRTxt;

    private void setMyBeacon(String code) {
        if (!code.equals("")) {
            SharedPreferenceHelper.setSharedStringPref(this, "myBeacon", code);
            Toast.makeText(this, "Your iBeacon has been registered.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Please scan your QR Code.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        QRTxt = (TextView) findViewById(R.id.QR_Code_txt);

        String myBeaconValue = SharedPreferenceHelper.getSharedStringPref(this, "myBeacon", "");
        if (!myBeaconValue.equals("")) {
            QRTxt.setText("Your iBeacon has been registered.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_QR_VALUE) {

            String QRCode = data.getStringExtra("QR");
            if (QRCode!=null) {
                QRTxt.setText("Your iBeacon has been registered.");
                setMyBeacon(QRCode);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scanBeacon:
                Intent intent = new Intent(this, ScannerActivity.class);
                startActivityForResult(intent, GET_QR_VALUE);
                return true;
            default:
                return false;
        }
    }
}
