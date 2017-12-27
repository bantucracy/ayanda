package sintulabs.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sabzo on 12/20/17.
 */

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener peerListListener;
    private Handler peerHandler;
    private List peers = new ArrayList();
    private WifiP2pDevice pi = new WifiP2pDevice();
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Handler peerHandler) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.peerHandler = peerHandler;
        setListener();
    }

    private void setListener() {
        connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {

                // InetAddress from WifiP2pInfo struct.
                InetAddress groupOwnerAddress = info.groupOwnerAddress;

                if (info.groupFormed && info.isGroupOwner) {
                    Log.d("Debug", "I'm GO, which shouldn't be the case");
                } else if (info.groupFormed) {
                    Log.d("Debug", "Connected to GO: " + info.groupOwnerAddress);
                }
            }
        };
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peerList) {
                        peers.clear();
                        peers.addAll(peerList.getDeviceList());
                        Message m = Message.obtain();
                        m.obj = peers;
                        // TODO secondary Filter of devices with registered service
                        //peerHandler.sendMessage(m);
                        // find PiDroid SSID
                        for (int i = 0; i < peers.size(); i++) {
                            WifiP2pDevice dev = (WifiP2pDevice) peers.get(i);
                        }
                    }
                });
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                mManager.requestConnectionInfo(mChannel, connectionInfoListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
