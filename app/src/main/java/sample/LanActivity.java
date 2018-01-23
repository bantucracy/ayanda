package sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import sintulabs.p2p.Ayanda;
import sintulabs.p2p.ILan;
import sintulabs.p2p.Lan;
import sintulabs.p2p.WifiDirect;

/**
 * Created by sabzo on 1/10/18.
 */

public class LanActivity extends AppCompatActivity {

    private WifiDirect p2p;
    private ListView lvDevices;
    private List peers = new ArrayList();
    private ArrayAdapter<String> peersAdapter = null;
    private List peerNames = new ArrayList();

    // Buttons
    private Button btnLanAnnounce;
    private Button btnLanDiscover;
    // LAN
    private Ayanda a;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
        setListeners();
        a = new Ayanda(this, null, new ILan() {
            @Override
            public void deviceListChanged() {
                peers.clear();
                peerNames.clear();
                peersAdapter.clear();

                peers.addAll(a.lanGetDeviceList());
                for (int i = 0; i < peers.size(); i++) {
                    Lan.Device d = (Lan.Device) peers.get(i);
                    peersAdapter.add(d.getName());
                }
            }
        }, null);
    }


    private void createView() {
        setContentView(R.layout.activity_main);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // buttons
        btnLanAnnounce = (Button) findViewById(R.id.btnLanAnnounce);
        btnLanDiscover = (Button) findViewById(R.id.btnLanDiscover);
        // ListView
        lvDevices = (ListView) findViewById(R.id.lvDevices);
        peersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNames);
        lvDevices.setAdapter(peersAdapter);
    }



    private void setListeners() {
        View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnLanAnnounce:
                        a.lanAnnounce();
                        break;
                    case R.id.btnLanDiscover:
                        a.lanDiscover();
                        break;
                }
            }
        };

        btnLanAnnounce.setOnClickListener(btnClick);
        btnLanDiscover.setOnClickListener(btnClick);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.miBt:
                startActivity(new Intent(this, BluetoothActivity.class ));
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
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    protected void onStop() {
        super.onStop();
        a.lanStopAnnouncement();
        a.lanStopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}