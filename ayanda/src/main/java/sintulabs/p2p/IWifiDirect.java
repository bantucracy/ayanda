package sintulabs.p2p;

import android.content.Intent;

import java.net.InetAddress;

/**
 * Created by sabzo on 1/21/18.
 */

public interface IWifiDirect {
    void wifiP2pStateChangedAction(Intent intent);
    void wifiP2pPeersChangedAction(); // all wifip2p peers
    void wifiP2pAyandaPeersChangedAction(); // all "ayanda" wifip2p peers determined by this library
    void wifiP2pConnectionChangedAction(Intent intent);
    void wifiP2pThisDeviceChangedAction(Intent intent);
    void onConnectedAsServer(Server server);
    void onConnectedAsClient(InetAddress groupOwnerAddress);
}
