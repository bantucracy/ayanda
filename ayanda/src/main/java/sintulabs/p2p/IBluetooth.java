package sintulabs.p2p;

import android.content.Intent;

/**
 * Created by sabzo on 1/21/18.
 */

public interface IBluetooth {
    // ACTION_DISCOVERY_STARTED
    void actionDiscoveryStarted(Intent intent);
    // ACTION_DISCOVERY_FINISHED
    void actionDiscoveryFinished(Intent intent);
    // ACTION_SCAN_MODE_CHANGED
    void stateChanged(Intent intent);
    // ACTION_STATE_CHANGED
    void scanModeChange(Intent intent);
    // Bluethooth.Device ACTION_FOUND
    void actionFound(Intent intent);
}
