package sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import sintulabs.p2p.Lan;
import sintulabs.p2p.WifiDirect;

/**
 * Created by sabzo on 1/10/18.
 */

public class ActivityMain extends AppCompatActivity {


    private WifiDirect p2p;
    private ListView lvDevices;
    private List peers = new ArrayList();
    private ArrayAdapter<String> peersAdapter = null;
    private List peerNames = new ArrayList();
    private Handler peerHandler;
    // Buttons
    private Button btnLanAnnounce;
    private Button btnLanDiscover;
    // LAN
    Lan lan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
        setHandler();
        setListeners();
        registerReceivers();
        // p2p = new WifiDirect(null, null, this, peerHandler);
        lan = new Lan(this);
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


    private void setHandler() {
        peerHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                peers.clear();
                //
                peers.addAll((List<WifiP2pDevice>) msg.obj);
                peerNames.clear();
                for (int i = 0; i < peers.size(); i++) {
                    WifiP2pDevice device = (WifiP2pDevice) peers.get(i);
                    peersAdapter.add(device.deviceName);
                }

                return true;
            }
        });
    }

    private void setListeners() {
        View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnLanAnnounce:
                        lan.announce();
                        break;
                    case R.id.btnLanDiscover:
                        lan.discover();
                        break;
                }
            }
        };

        btnLanAnnounce.setOnClickListener(btnClick);
        btnLanDiscover.setOnClickListener(btnClick);

        /*lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                WifiP2pDevice device = (WifiP2pDevice) peers.get(pos);
                p2p.connect(device);
            }
        }); */
    }

    /* register the broadcast receiver with the intent values to be matched */


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Define the callback for what to do when number of devices is updated
    private BroadcastReceiver lanDeviceNumReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            peers.clear();
            peerNames.clear();
            peersAdapter.clear();

            peers.addAll(lan.getDeviceList());
            for (int i = 0; i < peers.size(); i++) {
                Lan.Device d = (Lan.Device) peers.get(i);
                peersAdapter.add(d.getName());
            }
        }

    };


    private void registerReceivers() {
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(Lan.LAN_DEVICE_NUM_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(lanDeviceNumReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //p2p.registerReceivers();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        // p2p.unregisterReceiver();

    }
    @Override
    protected void onStop() {
        super.onStop();
        lan.stopAnnouncement();
        lan.stopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}