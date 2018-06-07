package sintulabs.p2p;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by sabzo on 1/19/18.
 */

public class Ayanda {
    private Bluetooth bt;
    private Lan lan;
    private WifiDirect wd;

    private Context context;

    /**
     * Ayanda is a class that discovers and interacts with nearby devices that support
     * Network Service Discovery (NSD), WiFi Direct, and  Bluetooth
     * @param context The Activity/Application Context
     * @param iBluetooth An interface to handle Bluetooth events
     * @param iLan An interface to handle LAN (NSD/Bonjour/ZeroConfig/etc.,) events
     * @param iWifiDirect An interface to handle Wifi Direct events
     */
    public Ayanda(Context context, IBluetooth iBluetooth, ILan iLan, IWifiDirect iWifiDirect) {
        this.context = context;
        if (iBluetooth != null) {
            bt = new Bluetooth(context, iBluetooth);
        }
        if (iLan != null) {
            lan = new Lan(context, iLan);
        }
        if (iWifiDirect != null) {
            wd = new WifiDirect(context, iWifiDirect);
        }
    }

    public void connectToDevice (Ayanda.Device device)
    {
        if (device.getType() == Device.TYPE_BLUETOOTH)
            btConnect((BluetoothDevice)device.getServiceInfo());
        else if (device.getType() == Device.TYPE_WIFI_P2P)
            wdConnect((WifiP2pDevice) device.getServiceInfo());
        else if (device.getType() == Device.TYPE_WIFI_LAN) {
            //no special connection needed
        }
    }

    /**
     * Discover nearby devices that have made themselves detectable via blue Bluetooth.
     * Discovered devices are stored in a collection of devices found.
     */
    public void btDiscover() {
        bt.discover();
    }

    /**
     * Connects to a discovered bluetooth device. Role: Client
     * @param device Bluetooth Device
     */
    public void btConnect(BluetoothDevice device) {
       bt.connect(device);
    }


    /**
     * Connects to a discovered bluetooth device. Role: Client
     * @param device Bluetooth Device
     */
    public void btConnect(Ayanda.Device device) {
        bt.connect((BluetoothDevice)device.getServiceInfo());
    }

    /**
     * Register a Bluetooth Broadcast Receiver.
     * This method must be called to detect Bluetooth events. The iBluetooth interface
     * exposes Bluetooth events.
     */
    public void btRegisterReceivers() {
        bt.registerReceivers();
    }

    /**
     * Unregisters Bluetooth Broadcast Receiver.
     * Must be called when Activity/App stops/closes
     */
    public void btUnregisterReceivers() {
        bt.unregisterReceivers();
    }

    /**
     * Announce device's presence via Bluetooth
     */
    public void btAnnounce() {
        bt.announce();
    }


    public HashMap<String, BluetoothDevice> btGetDevices() {
        return bt.getDeviceList();
    }

    /**
     * Send data from this device to a connected bluetooth device
     * @param device
     * @param bytes
     */
    public void btSendData(BluetoothDevice device, byte[] bytes) throws IOException {
        bt.sendData(device, bytes);
    }


    public boolean isLanEnabled ()
    {
        return lan != null;
    }

    public boolean isBtEnabled ()
    {
        return bt != null;
    }

    public void lanAnnounce() {
        lan.announce();
    }
    /*
        Discover nearby devices using LAN:
        A device can register a service on the network and other devices connected on the network
        will be able to detect it.
     */
    public void lanDiscover() {
        lan.discover();
    }

    public void lanStopAnnouncement() {
        lan.stopAnnouncement();
    }

    public void lanStopDiscovery() {
        lan.stopDiscovery();
    }
    public List<Device> lanGetDeviceList() {
      return lan.getDeviceList();
    }

    /* Wifi Direct Methods */

    /**
     *
     * @param device to send data to
     * @param bytes array of data to send
     */
    public void wdSendData(WifiP2pDevice device, byte[] bytes) {
        wd.sendData(device, bytes);
    }

    /**
     * Connect to a WifiDirect device
     * @param device
     */
    public void wdConnect(WifiP2pDevice device) {
        wd.connect(device);
    }


    /**
     * Connect to a WifiDirect device
     * @param device
     */
    public void wdConnect(Ayanda.Device device) {
        wd.connect(device);
    }

    /**
     * Discover nearby WiFi Direct enabled devices
     */
    public void wdDiscover() {
        wd.discover();
    }

    public void wdRegisterReceivers() {
        wd.registerReceivers();
    }

    public boolean isWdEnabled  ()
    {
        return wd != null;
    }

    public void wdUnregisterReceivers() {
        wd.unregisterReceivers();
    }

    public ArrayList<Ayanda.Device> wdGetDevicesDiscovered() {
        return wd.getDevicesDiscovered();
    }

    /**
     *  Add a user defined Server class to respond to client calls
     * @param server A descendant of the server class
     */
    public void setServer(IServer server) {

        if (lan != null) {
            lan.setServer(server);
        }
        if (wd != null) {
            wd.setServer(server);
        }
    }

    public static int findOpenSocket() throws java.io.IOException {
        // Initialize a server socket on the next available port.
        ServerSocket serverSocket = new ServerSocket(0);
        // Store the chosen port.
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }

    public static class Device {
        private InetAddress host;
        private Integer port;
        private Object serviceInfo;

        private String address;
        private String name;

        private int type = -1;
        public final static int TYPE_WIFI_P2P = 1;
        public final static int TYPE_WIFI_LAN = 2;
        public final static int TYPE_BLUETOOTH = 3;

        private final static String AYANDA_DEVICE_IDENTIFIER = "Ayanda.";

        public HashMap<String,Device> altDevices = new HashMap<>();

        public Device(BluetoothDevice device) {

            this.name = parseName(device.getName());
            this.address = device.getAddress();

            type = TYPE_BLUETOOTH;
            serviceInfo = device;
        }

        public Device(WifiP2pDevice device) {

            this.name = parseName(device.deviceName);
            this.address = device.deviceAddress;

            type = TYPE_WIFI_P2P;
            serviceInfo = device;
        }

        public Device(NsdServiceInfo serviceInfo) {

            this.port = serviceInfo.getPort();
            this.host = serviceInfo.getHost();
            this.serviceInfo = serviceInfo;
            this.name = parseName(serviceInfo.getServiceName());
            if (host != null)
                this.address = host.getHostAddress();

            type = TYPE_WIFI_LAN;
        }

        private String parseName (String deviceName)
        {
            if (!TextUtils.isEmpty(deviceName))
                return deviceName.replace(AYANDA_DEVICE_IDENTIFIER,"");
            else
                return deviceName;
        }

        public void addAltDevice (Device device)
        {
            altDevices.put(device.getName()+device.getType(),device);
        }

        public Collection<Device> getAltDevices ()
        {
            return altDevices.values();
        }

        public Object getServiceInfo ()
        {
            return serviceInfo;
        }

        public InetAddress getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        public String getName() {
            return name;
        }

        public int getType () {
            return type;
        }

        public String getTypeReadable () {

            switch (type)
            {
                case TYPE_WIFI_P2P:
                    return "Wifi P2P";
                case TYPE_WIFI_LAN:
                    return "Wifi Hotspot";
                case TYPE_BLUETOOTH:
                    return "Bluetooth";
            }

            return null;
        }

        public String getDeviceId () {
            return name + "@" + address;
        }

    }
}
