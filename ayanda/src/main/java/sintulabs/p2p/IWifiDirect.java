package sintulabs.p2p;

import android.content.Intent;

/**
 * Created by sabzo on 1/21/18.
 */

public interface IWifiDirect {
    void wifiP2pStateChangedAction(Intent intent);
    void wifiP2pPeersChangedAction();
    void wifiP2pConnectionChangedAction(Intent intent);
    void wifiP2pThisDeviceChangedAction(Intent intent);
}
