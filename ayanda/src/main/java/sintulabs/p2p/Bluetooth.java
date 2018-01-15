package sintulabs.p2p;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;


/**
 * Created by sabzo on 1/14/18.
 */

public class Bluetooth extends P2P {
    private Context context;
    BluetoothAdapter mBluetoothAdapter;


    public Bluetooth(Context context) {
        this.context = context;
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected Boolean isSupported() {
       return  (mBluetoothAdapter == null)? false : true;
    }

    @Override
    protected Boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /* Enable Bluetooth if it's supported but not yet enabled */
    @Override
    public void announce() {
        if ( isSupported() && !isEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            context.startActivity(discoverableIntent);
        }
    }

    @Override
    public void discover() {

    }

    @Override
    protected void disconnect() {

    }

    @Override
    protected void send() {

    }

    @Override
    protected void cancel() {

    }
}
