package sample;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import sintulabs.ayanda.R;
import sintulabs.p2p.Bluetooth;

/**
 * Created by sabzo on 1/14/18.
 */

public class BluetoothActivity extends AppCompatActivity {
    private Button btnAnnounce;
    private Button btnDiscover;
    private Bluetooth bt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bt = new Bluetooth(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                Bluetooth.BT_PERMISSION_REQUEST_LOCATION);
        setContentView(R.layout.bluetooth_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnAnnounce = (Button) findViewById(R.id.btnBtAnnounce);
        btnDiscover = (Button) findViewById(R.id.btnBtDiscover);
        setListeners();
    }

    private void setListeners() {
        View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnBtAnnounce:
                        bt.announce();
                        break;
                    case R.id.btnBtDiscover:
                        bt.discover();
                        break;
                }
            }
        };
        btnAnnounce.setOnClickListener(btnClick);
        btnDiscover.setOnClickListener(btnClick);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.unregisterReceiver();
    }
}
