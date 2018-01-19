package sample;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sintulabs.ayanda.R;
import sintulabs.p2p.Bluetooth;
import sintulabs.p2p.Lan;
import sintulabs.p2p.WifiDirect;

/**
 * Created by sabzo on 1/14/18.
 */

public class BluetoothActivity extends AppCompatActivity {
    private Button btnAnnounce;
    private Button btnDiscover;
    private Bluetooth bt;
    private ListView lvBtDeviceNames;
    private ArrayAdapter<String> peersAdapter = null;
    private List peerNames = new ArrayList();
    private List peers = new ArrayList();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bt = new Bluetooth(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                Bluetooth.BT_PERMISSION_REQUEST_LOCATION);
        setContentView(R.layout.bluetooth_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createView();
        setListeners();
        registerReceivers();
    }

    private void createView() {
        lvBtDeviceNames = (ListView) findViewById(R.id.lvBtDeviceNames);
        peersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNames);
        lvBtDeviceNames.setAdapter(peersAdapter);
        btnAnnounce = (Button) findViewById(R.id.btnBtAnnounce);
        btnDiscover = (Button) findViewById(R.id.btnBtDiscover);
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

    // Define the callback for what to do when number of devices is updated
    private BroadcastReceiver btDeviceNamesFound = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            peersAdapter.clear();
            peersAdapter.addAll(bt.getDeviceNamesDiscovered());
        }

    };


    private void registerReceivers() {
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(bt.BT_DEVICE_FOUND);
        LocalBroadcastManager.getInstance(this).registerReceiver(btDeviceNamesFound, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.miLan:
                startActivity(new Intent(this, LanActivity.class ));
                finish();
                break;
            case R.id.miWd:
                startActivity(new Intent(this, WifiDirectActivity.class ));
                finish();
                break;
        }
        return true;
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
