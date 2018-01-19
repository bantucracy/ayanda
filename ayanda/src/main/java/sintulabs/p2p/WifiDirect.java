package sintulabs.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.util.Log;

/**
 * WiFi Direct P2P Class for detecting and connecting to nearby devices
 */
public class WifiDirect extends P2P {
    private static WifiP2pManager wifiP2pManager;
    private static WifiP2pManager.Channel wifiDirectChannel;
    private Context context;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private Handler peerHandler;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    public WifiDirect(Context context, Handler peerHandler) {
        this.context = context;
        this.peerHandler = peerHandler;
        initializeWifiDirect();
        // IntentFilter for receiver
        createIntent();
        createReceiver();
    }

    private void createIntent() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
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

    @Override
    public void announce() {

    }

    @Override
    public void discover() {
        discoverPeers();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void send() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public Boolean isSupported() {
        return null;
    }

    @Override
    public Boolean isEnabled() {
        return null;
    }

}