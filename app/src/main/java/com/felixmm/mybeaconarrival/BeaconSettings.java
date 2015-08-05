package com.felixmm.mybeaconarrival;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class BeaconSettings extends AppCompatActivity {

    static final int GET_QR_VALUE = 1;

    TextView QRTxt;

    public void launchQRActivity(View v) {
        Intent intent = new Intent(v.getContext(), ScannerActivity.class);
        startActivityForResult(intent, GET_QR_VALUE);
    }

    public void setMyBeacon(View v) {
        if (!QRTxt.getText().toString().equals("")) {
            SharedPreferenceHelper.setSharedStringPref(this, "myBeacon", QRTxt.getText().toString());
            Toast.makeText(this, "Your beacon has been registered.",Toast.LENGTH_SHORT).show();
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
            QRTxt.setText(myBeaconValue);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_QR_VALUE) {

            String result = data.getStringExtra("QR");
            if (result!=null) QRTxt.setText(result);
        }
    }
}
