package sintulabs.p2p;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
import static android.bluetooth.BluetoothAdapter.STATE_ON;


/**
 * Created by sabzo on 1/14/18.
 */

public class Bluetooth extends P2P {
    private Context context;
    BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    public static String BT_DEVICE_FOUND = "4000";
    public static Integer REQUEST_ENABLE_BT = 1;
    public static Integer BT_PERMISSION_REQUEST_LOCATION = 4444;
    public static Integer BT_ENABLED = 3000;
    private Boolean discoveryInitiated = false;
    private Set<String> deviceNamesDiscovered;
    private List<Device> deviceList;

    private IBluetooth iBluetooth;


    public Bluetooth(Context context, IBluetooth iBluetooth) {
        this.context = context;
        this.iBluetooth = iBluetooth;
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        deviceNamesDiscovered = new HashSet<>();
        deviceList = new ArrayList<>();
        createIntentFilter();
        createReceiver();
        // ensure to register and unregister receivers
    }

    @Override
    public Boolean isSupported() {
       return  (mBluetoothAdapter == null)? false : true;
    }

    @Override
    public Boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    private void enable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((Activity)context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    /* Enable Bluetooth if it's supported but not yet enabled */
    @Override
    public void announce() {
        if ( isSupported()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            context.startActivity(discoverableIntent);
        }
    }

    private void createIntentFilter() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_SCAN_MODE_CHANGED);
    }

    /* Bluetooth event handler */
    private void createReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                switch (action) {
                    case ACTION_DISCOVERY_STARTED:
                        actionDiscoveryStarted(intent);
                        break;
                    case ACTION_DISCOVERY_FINISHED:
                        actionDiscoveryFinished(intent);
                        break;
                    case ACTION_SCAN_MODE_CHANGED:
                        scanModeChange(intent);
                        break;
                    case ACTION_STATE_CHANGED:
                        stateChanged(intent);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        deviceFound(intent);
                        break;
                }
            }

            private void scanModeChange(Intent intent) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    // discoverable and can be connected to
                    case SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(context, "Device is connectable and discoverable", Toast.LENGTH_SHORT).show();
                        Log.d(TAG_DEBUG, "Device is connectable and discoverable");
                        break;
                    // not discoverable but connectable from previously paired devices
                    case SCAN_MODE_CONNECTABLE:
                        break;
                }
            }

            // Discovery is quick and limited (about 12 seconds)
            private void actionDiscoveryStarted(Intent intent) {
                Log.d(TAG_DEBUG, "Discovery started");
                iBluetooth.actionDiscoveryStarted(intent);
            }
            // Calls after BT finishes scanning (12 seconds)
            private void actionDiscoveryFinished(Intent intent) {
                discoveryInitiated = false;
                Log.d(TAG_DEBUG, "Discovery finished");
                iBluetooth.actionDiscoveryFinished(intent);
            }

            /* Bluetooth enabled/disabled */
            private void stateChanged(Intent intent) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                   switch (state) {
                    // Bluetooth state changed: Is it On?
                    case STATE_ON:
                        if (discoveryInitiated && !mBluetoothAdapter.startDiscovery()) {
                            Log.d(TAG_DEBUG, "unable to start bluetooth discovery");
                        };
                        break;
                }
                iBluetooth.stateChanged(intent);
            }

            private void deviceFound(Intent intent) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Device d = new Device(device);
                deviceList.add(d);
                String deviceName = d.getDeviceName() == null? d.getDeviceAddress(): d.getDeviceName();
                deviceNamesDiscovered.add(deviceName);
                iBluetooth.actionFound(intent);
            }
        };
    }

    /* Register/unregister Receiver */

    public void registerReceivers() {
        context.registerReceiver(receiver, intentFilter);
    }

    public void unregisterReceivers() {
        context.unregisterReceiver(receiver);
    }


    @Override
    public void discover() {
        if ( isSupported()) {
            discoveryInitiated = true;
            if (!isEnabled()) {
                enable();
            } else {
                if (!mBluetoothAdapter.startDiscovery()) {
                    Log.d(TAG_DEBUG, "unable to start bluetooth discovery");
                };
            }
        }
    }

    public List<Device> getDeviceList() {
        return deviceList;
    }

    public Set<String> getDeviceNamesDiscovered() {
       return deviceNamesDiscovered;
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

    public static class Device {
        private BluetoothDevice device;
        private String deviceName;
        private String deviceAddress; // MAC address

        public Device(BluetoothDevice device) {
            this.device = device;
            deviceName = device.getName();
            deviceAddress = device.getAddress(); // MAC address
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getDeviceAddress() {
            return deviceAddress;
        }

    }
}
