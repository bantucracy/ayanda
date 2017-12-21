package sintulabs.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * A Wifi Direct implementation between devices that support WiFi Direct
 * Created by sabzo on 12/20/17.
 */

public class WifiDirect {
    private static WifiP2pManager wifiP2pManager;
    private static WifiP2pManager.Channel wifiDirectChannel;
    private Context context;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private Handler peerHandler;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private final String TAG_DEBUG = "ayanda_bug";
    private final Integer PORT = 8080;
    private WifiP2pDnsSdServiceInfo serviceInfo;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    public final static String SERVICE_INSTANCE = "ayanda";
    public final static String SERVICE_REG_TYPE = "_presence._tcp";

    private void createIntent() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public WifiDirect(String ssid, String password, Context context, Handler peerHandler) {
        this.context = context;
        this.peerHandler = peerHandler;
        initializeWifiDirect();
        // IntentFilter for receiver
        createIntent();
        createReceiver();
        discoverPeers();

    }

    // Announce Wifi Direct service
    private void announceWiFiDirectService() {
        Log.d(TAG_DEBUG, "Setting up ServiceAnnouncment");
        Map<String, String> txtRecords = new HashMap<String, String>();

        txtRecords.put("identity_instance", SERVICE_INSTANCE);
        txtRecords.put("port", Integer.toString(PORT));
        txtRecords.put("device_id", "ay_" + (int) (Math.random() * 1000));

        // Create Service Info Object containing the service details
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, txtRecords);
        // Publish local service SERVICE_INSTANCE with Service Tye  _http._tcp
        wifiP2pManager.addLocalService(wifiDirectChannel, serviceInfo,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG_DEBUG, "Service announcing !");
                        writeMessage("Service announcing !");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG_DEBUG, "Service announcement failed: " + i);
                        writeMessage("Service announcement failed: " + i);
                    }
                });
    }


    // Create WifiP2pManager and Channel
    private void initializeWifiDirect() {
        wifiP2pManager = (WifiP2pManager) context.getSystemService(context.WIFI_P2P_SERVICE);
        wifiDirectChannel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                // On Disconnect reconnect again
                initializeWifiDirect();
            }
        });
    }

    private void createReceiver() {
        receiver = new WifiDirectBroadcastReceiver(wifiP2pManager, wifiDirectChannel, peerHandler);
    }

    public void registerReceivers() {
        context.registerReceiver(receiver, intentFilter);
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(receiver);
    }

    // look for nearby peers
    private void discoverPeers() {
        wifiP2pManager.discoverPeers(wifiDirectChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("Debug", "failed to look for pears: " + reasonCode);
            }
        });
    }


    public void connect(WifiP2pDevice device) {
        // Picking the first device found on the network.

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        wifiP2pManager.connect(wifiDirectChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(context, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void writeMessage(String msg) {
        Message message = Message.obtain();
        message.obj = msg;
        peerHandler.sendMessage(message);
    }
}
