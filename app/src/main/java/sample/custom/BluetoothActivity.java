package sample.custom;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sintulabs.ayanda.R;
import sintulabs.p2p.Ayanda;
import sintulabs.p2p.Bluetooth;
import sintulabs.p2p.IBluetooth;

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
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();

    private Ayanda a;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = new Ayanda(this, new IBluetooth() {


            public String getPublicName ()
            {
                return "Ayanda Bluetooth Device";
            }

            @Override
            public void actionDiscoveryStarted(Intent intent) {

            }

            @Override
            public void actionDiscoveryFinished(Intent intent) {

            }

            @Override
            public void stateChanged(Intent intent) {

            }

            @Override
            public void scanModeChange(Intent intent) {

            }

            @Override
            public void actionFound(Intent intent) {
                peersAdapter.clear();
                devices = a.btGetDevices();

                peersAdapter.addAll(devices.keySet());
            }

            @Override
            public void dataRead(byte[] bytes, int length) {
                String readMessage = new String(bytes, 0, length);
                Toast.makeText(BluetoothActivity.this, readMessage, Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void connected(BluetoothDevice device) {
                String message = "Hello World";
                try {
                    a.btSendData(device, message.getBytes()); // maybe a class for a device that's connected
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null, null);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                Bluetooth.BT_PERMISSION_REQUEST_LOCATION);
        setContentView(R.layout.bluetooth_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createView();
        setListeners();
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
                        a.btAnnounce();
                        break;
                    case R.id.btnBtDiscover:
                        a.btDiscover();
                        break;
                }
            }
        };
        AdapterView.OnItemClickListener clickPhone = new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                BluetoothDevice device = devices.get(peerNames.get(pos));
                a.btConnect(device);
            }
        };

        btnAnnounce.setOnClickListener(btnClick);
        btnDiscover.setOnClickListener(btnClick);
        lvBtDeviceNames.setOnItemClickListener(clickPhone);
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
    protected void onResume() {
        super.onResume();
        a.btRegisterReceivers();
    }



    @Override
    protected void onPause() {
        super.onPause();
        a.btUnregisterReceivers();
    }

}
