package sintulabs.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import static android.bluetooth.BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;

/**
 * Created by sabzo on 1/15/18.
 */

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_SCAN_MODE_CHANGED:
                discoverableModeChange(intent);
                ;
        }
    }

    /* Fires when bluetooth has been turned on/off */
    private void discoverableModeChange(Intent intent) {
        String s = "hi";
    }
}
