package sintulabs.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_DISABLED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED;

/**
 * WiFi Direct P2P Class for detecting and connecting to nearby devices
 */
public class WifiDirect extends P2P {
    private static WifiP2pManager wifiP2pManager;
    private static WifiP2pManager.Channel wifiDirectChannel;
    private Context context;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private Boolean wiFiP2pEnabled = false;
    private ArrayList <WifiP2pDevice> peers = new ArrayList();
    private IWifiDirect iWifiDirect;

    public WifiDirect(Context context, IWifiDirect iWifiDirect) {
        this.context = context;
        this.iWifiDirect = iWifiDirect;
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
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                        wifiP2pStateChangedAction(intent);
                        break;

                    case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                        wifiP2pPeersChangedAction();
                        break;

                    case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                        wifiP2pConnectionChangedAction(intent);
                        break;
                    case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                        // Respond to this device's wifi state changing
                        wifiP2pThisDeviceChangedAction(intent);
                        break;
                }
            }

        };
    }

    public void wifiP2pStateChangedAction(Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        switch (state) {
            case WIFI_P2P_STATE_DISABLED:
                wiFiP2pEnabled = false;
                break;
            case WIFI_P2P_STATE_ENABLED:
                wiFiP2pEnabled = true;
                break;
        }
        iWifiDirect.wifiP2pStateChangedAction(intent);
    }

    public void wifiP2pPeersChangedAction() {
        if (wifiP2pManager != null) {
            wifiP2pManager.requestPeers(wifiDirectChannel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peerList) {
                    peers.clear();
                    peers.addAll(peerList.getDeviceList());
                }
            });
        }
        iWifiDirect.wifiP2pPeersChangedAction();
    }

    public void wifiP2pConnectionChangedAction(Intent intent) {
        // Respond to new connection or disconnections
        if (wifiP2pManager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (networkInfo.isConnected()) {
            // We are connected with the other device, request connection
            // info to find group owner IP
            // wifiP2pManager.requestConnectionInfo(wifiDirectChannel, connectionInfoListener);
        }
        iWifiDirect.wifiP2pConnectionChangedAction(intent);
    }

    public void wifiP2pThisDeviceChangedAction(Intent intent) {
        iWifiDirect.wifiP2pThisDeviceChangedAction(intent);

    }
    public void registerReceivers() {
        context.registerReceiver(receiver, intentFilter);
    }

    public void unregisterReceivers() {
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

    /* Return devices discovered. Method should be called when WIFI_P2P_PEERS_CHANGED_ACTION
        is complete
     */
    public ArrayList<WifiP2pDevice> getDevicesDiscovered() {
        return peers;
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