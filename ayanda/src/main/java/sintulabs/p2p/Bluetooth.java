package sintulabs.p2p;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public static Integer REQUEST_ENABLE_BT = 1;
    public static Integer BT_PERMISSION_REQUEST_LOCATION = 4444;
    private Boolean discoveryInitiated = false;
    private Set<String> deviceNamesDiscovered;
    private List<Device> deviceList;
    Set<BluetoothDevice> pairedDevices;

    public static String UUID = "00001101-0000-1000-8000-00805F9B34AC"; // arbitrary
    public static String NAME = "Ayanda";

    /* Server */
    private ServerThread serverThread;

    /* Bluetooth Events Interface */
    private IBluetooth iBluetooth;


    public Bluetooth(Context context, IBluetooth iBluetooth) {
        this.context = context;
        this.iBluetooth = iBluetooth;
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        deviceNamesDiscovered = new HashSet<>();
        deviceList = new ArrayList<>();
        createServer();
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

    /**
     *  Announce Bluetooth service to Nearby Devices
     */
    @Override
    public void announce() {
        if (isSupported()) {
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

        };
    }

    private void getPairedDevices() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (device != null && device.getName() != null &&
                        (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA ||
                                device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA ||
                                device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART)) {
                    deviceFound(null);
                }
            }
        }
    }

    private void deviceFound(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Device d = new Device(device);
        deviceList.add(d);
        String deviceName = d.getDeviceName() == null ? d.getDeviceAddress() : d.getDeviceName();
        deviceNamesDiscovered.add(deviceName);
        iBluetooth.actionFound(intent);
    }

    /* Connect to a discovered device */
    private void connect(BluetoothDevice device) {
        new ConnectThread(device).start();
    }

    /*  Create Bluetooth Server to accept client connections from nearby devices */
    private void createServer() {
        if (serverThread == null) {
            serverThread = new ServerThread();
            serverThread.start();
        }
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
                getPairedDevices();
                if (!mBluetoothAdapter.startDiscovery()) {
                    Log.d(TAG_DEBUG, "unable to start bluetooth discovery");
                }
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


    /**
     * Write's data to a connected device using a Bluetooth RFCOMM channel
     * @param bytes
     * @throws IOException if for any reason current device can't write to a client
     */
    public void write(byte [] bytes) throws IOException {
        announce();
        createServer();
        serverThread.write(bytes);
    }

    /**
     * Represents a Bluetooth Device
     */
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

    /**
     *  Creates Sever to receive connections from other bluetooth devices
     */
    private class ServerThread extends Thread {
        // Server
        private BluetoothServerSocket btServerSocket;
        private BluetoothSocket btSocket;
        private Connection connection = null;

        public ServerThread() {
            try {
                btServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME,
                        java.util.UUID.fromString(UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                btSocket = btServerSocket.accept();
                connection = new Connection(btSocket);
                // client has connected
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (btSocket != null) {
            }
        }

        /* Write to connected Client */
        public void write(final byte[] bytes) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // loop until connection becomes available
                    while (true) {
                        if (connection != null) {
                            try {
                                connection.write(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }).start();
        }

        // close open thread
        public void close() {
            try {
                btServerSocket.close();
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  Connect as a Client to a nearby Bluetooth Device acting as a server
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket socket = null;
        private Connection connection = null;

        public ConnectThread(BluetoothDevice device) {
            try {
                socket = device.createRfcommSocketToServiceRecord(
                        java.util.UUID.fromString(UUID));
                connection = new Connection(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (socket != null) {
                try {
                    socket.connect();
                    handleConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void handleConnection() {
            if (connection != null) {
                try {
                    connection.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG_DEBUG, "Unable to create a connection");
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Represents an Active Connection between this device and another device
     */
    private class Connection {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte[] buffer; // buffer for stream

        public Connection(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG_DEBUG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG_DEBUG, "Error occurred when creating output stream", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        /* Write bytes to connected device */
        public void write(byte[] bytes) throws IOException {
            outputStream.write(bytes);
        }

        /* Read data from connected device */
        public void read() throws IOException {
            buffer = new byte[1024];
            int numBytesRead = inputStream.read(buffer);
        }
    }

}
