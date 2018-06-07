package sintulabs.p2p;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_DISABLED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED;
import static sintulabs.p2p.Lan.SERVICE_TYPE;

/**
 * WiFi Direct P2P Class for detecting and connecting to nearby devices
 */
public class WifiDirect extends P2P {
    private static WifiP2pManager wifiP2pManager;
    private static WifiP2pManager.Channel wifiDirectChannel;
    private Context context;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private Boolean wiFiP2pEnabled = false;
    private Boolean isGroupOwner = false;
    private InetAddress groupOwnerAddress;
    private HashMap<String, WifiP2pDevice> peers = new HashMap<>();
    private IWifiDirect iWifiDirect;

    private Boolean isClient = false;
    private Boolean isServer = false;

    private static final String SERVICE_INSTANCE = "Ayanda";

    private final static String TAG = "AyandaWifiDirect";

    private IServer mServer = null;

    /**
     * Creates a WifiDirect instance
     *
     * @param context     activity/application contex
     * @param iWifiDirect an inteface to provide callbacks to WiFi Direct events
     */
    public WifiDirect(Context context, IWifiDirect iWifiDirect) {
        this.context = context;
        this.iWifiDirect = iWifiDirect;
        initializeWifiDirect();
        // IntentFilter for receiver
        createIntent();
        createReceiver();
    }

    public void setServer (IServer server)
    {
        mServer = server;
    }


    /**
     * Create intents for default WiFi direct actions
     */
    private void createIntent() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    /**
     * Create WifiP2pManager and Channel
     */
    private void initializeWifiDirect() {
        wifiP2pManager = (WifiP2pManager) context.getSystemService(context.WIFI_P2P_SERVICE);
        wifiDirectChannel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                // On Disconnect reconnect again
                initializeWifiDirect();
            }
        });

        setDeviceName(SERVICE_NAME_BASE + iWifiDirect.getPublicName(),wifiP2pManager,wifiDirectChannel);


    }

    private void setDeviceName(String new_name, WifiP2pManager manager, WifiP2pManager.Channel channel)
    {
        try {
            Method m = manager.getClass().getMethod(
                    "setDeviceName",
                    new Class[] { WifiP2pManager.Channel.class, String.class,
                            WifiP2pManager.ActionListener.class });

            m.invoke(manager, channel, new_name, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    //Code for Success in changing name
                }

                public void onFailure(int reason) {
                    //Code to be done while name change Fails
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * receiver for WiFi direct hardware events
     */
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

    /**
     * When Wifi Direct is enabled/disabled. Propagates event to WiFi Direct interface
     * @param intent
     */
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

    /**
     * When new peers are discovered
     */
    public void wifiP2pPeersChangedAction() {
        if (wifiP2pManager != null) {
            wifiP2pManager.requestPeers(wifiDirectChannel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peerList) {

                    for (WifiP2pDevice p2pdevice: peerList.getDeviceList()) {
                        if (p2pdevice.deviceName.startsWith(SERVICE_NAME_BASE))
                            peers.put(p2pdevice.deviceName,p2pdevice);
                    }

                    iWifiDirect.wifiP2pPeersChangedAction();
                }
            });
        }

    }

    /**
     * When connection is made/lost
     * @param intent
     */
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
            wifiP2pManager.requestConnectionInfo(wifiDirectChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                    if (wifiP2pInfo.groupFormed) {
                        isGroupOwner = wifiP2pInfo.isGroupOwner;
                        groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

                        startRegistrationAndDiscovery(getIpAddress(),mServer.getPort());

                        if (isGroupOwner) {
                            isServer = true;
                            onConnectedAsServer();
                        } else {
                            isClient = true;
                            onConnectedAsClient();
                        }
                    }
                }
            });
        }
        iWifiDirect.wifiP2pConnectionChangedAction(intent);
    }

    /**
     * This device connected as a group owner (server).
     */
    private void onConnectedAsServer() {

        iWifiDirect.onConnectedAsServer();
    }

    /**
     * This device connected as a client
     */
    private void onConnectedAsClient() {
        iWifiDirect.onConnectedAsClient(groupOwnerAddress);
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

    /**
     * look for nearby peers
     */

    private void discoverPeers() {
        if (!isLocationOn()) {
            enableLocation(context);
        }
        wifiP2pManager.discoverPeers(wifiDirectChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Debug", "success looking for peers");

            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("Debug", "failed to look for pears: " + reasonCode);
            }
        });
    }

    /**
     * Return devices discovered. Method should be called when WIFI_P2P_PEERS_CHANGED_ACTION
     is complete
     * @return Arraylist <WifiP2pDevice>
     */
    public ArrayList<Ayanda.Device> getDevicesDiscovered() {

        ArrayList<Ayanda.Device> alDevices = new ArrayList<>();

        for (WifiP2pDevice p2pDevice : peers.values())
        {
            Ayanda.Device aDevice = new Ayanda.Device(p2pDevice);
            alDevices.add(aDevice);
        }

        return alDevices;
    }

    private Ayanda.Device mDeviceConnected = null;

    /**
     * Connect to a nearby device
     * @param aDevice
     */
    public void connect(final Ayanda.Device aDevice) {

        WifiP2pDevice device = peers.get(aDevice.getName());
        if (device != null) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            wifiP2pManager.connect(wifiDirectChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                    mDeviceConnected = aDevice;
                }

                @Override
                public void onFailure(int reason) {
                    // todo if failure == 2
                    mDeviceConnected = null;
                }
            });
        }
    }

    /**
     * Connect to a nearby device
     * @param device
     */
    public void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        wifiP2pManager.connect(wifiDirectChannel,config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                // todo if failure == 2
            }
        });
    }

    /**f
     * Should be called when a connection has already been made to WifiP2pDevice
     * @param device
     * @param bytes
     */
    public void sendData(WifiP2pDevice device, byte[] bytes) {


    }

    /**
     * Android 8.0+ requires location to be turned on when discovering
     * nearby devices.
     * @return boolean
     */
    public boolean isLocationOn() {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Enable location
     * @param context
     */
    private void enableLocation(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Determines if device is connected and acting as a client
     * @return
     */
    public Boolean isClient() {
        return isClient;
    }

    /**
     * Determines if device is connected and acting as a server (GroupOwner)
     * @return
     */
    public Boolean getIsServer() {
       return isServer;
    }


    @Override
    public void announce() {}

    @Override
    public void discover() {
        discoverPeers();
    }

    /**
     * is Wifi Direct supported
     * @return
     */
    @Override
    public Boolean isSupported() {
        return null;
    }

    /**
     * is Wifi Direct enabled
     * @return
     */
    @Override
    public Boolean isEnabled() {
        return null;
    }

    class TransferConstants
    {
        public final static String KEY_BUDDY_NAME = "buddy";
        public final static String KEY_PORT_NUMBER = "port";
        public final static String KEY_DEVICE_STATUS = "status";
        public final static String KEY_WIFI_IP = "wifiip";
    }

    public void startRegistrationAndDiscovery(String serverIP, int port) {

        String player = SERVICE_NAME_BASE + iWifiDirect.getPublicName();

        Map<String, String> record = new HashMap<String, String>();
        record.put(TransferConstants.KEY_BUDDY_NAME, player == null ? Build.MANUFACTURER : player);
        record.put(TransferConstants.KEY_PORT_NUMBER, String.valueOf(port));
        record.put(TransferConstants.KEY_DEVICE_STATUS, "available");
        record.put(TransferConstants.KEY_WIFI_IP, serverIP);

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_TYPE, record);
        wifiP2pManager.addLocalService(wifiDirectChannel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.e(TAG, "ERRORCEPTION: Failed to add a service");
            }
        });
        discoverService();
    }

    WifiP2pDnsSdServiceRequest serviceRequest;

    private void discoverService() {

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */

        wifiP2pManager.setDnsSdResponseListeners(wifiDirectChannel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        Log.d(TAG, instanceName + "####" + registrationType);
                        // A service has been discovered. Is this our app?
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                            // yes it is
                            /**
                            WiFiP2pServiceHolder serviceHolder = new WiFiP2pServiceHolder();
                            serviceHolder.device = srcDevice;
                            serviceHolder.registrationType = registrationType;
                            serviceHolder.instanceName = instanceName;
                            connectP2p(serviceHolder);
                             **/
                        } else {
                            //no it isn't
                        }
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        boolean isGroupOwner = device.isGroupOwner();

                        String buddyName = record.get(TransferConstants.KEY_BUDDY_NAME).toString();
                        int peerPort = Integer.parseInt(record.get(TransferConstants.KEY_PORT_NUMBER).toString());
                        String peerIP= record.get(TransferConstants.KEY_WIFI_IP).toString();


                        Log.v(TAG, Build.MANUFACTURER + ". peer port received: " + peerPort);
                        /**
                        if (peerIP != null && peerPort > 0 && !isConnectionInfoSent) {
                            String player = record.get(TransferConstants.KEY_BUDDY_NAME).toString();

                            DataSender.sendCurrentDeviceData(LocalDashWiFiP2PSD.this,
                                    peerIP, peerPort, true);
                            isWDConnected = true;
                            isConnectionInfoSent = true;
                        }**/

                    }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        wifiP2pManager.addServiceRequest(wifiDirectChannel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d(TAG, "ERRORCEPTION: Failed adding service discovery request");
                    }
                });
        wifiP2pManager.discoverServices(wifiDirectChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Log.d(TAG, "Service discovery failed: " + arg0);
            }
        });
    }

    public static String getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            /*
             * for (NetworkInterface networkInterface : interfaces) { Log.v(TAG,
             * "interface name " + networkInterface.getName() + "mac = " +
             * getMACAddress(networkInterface.getName())); }
             */

            for (NetworkInterface intf : interfaces) {
                /**
                if (!getMACAddress(intf.getName()).equalsIgnoreCase(
                        Globals.thisDeviceAddress)) {
                    // Log.v(TAG, "ignore the interface " + intf.getName());
                    // continue;
                }**/
                if (!intf.getName().contains("p2p"))
                    continue;

                Log.v(TAG,
                        intf.getName() + "   " + getMACAddress(intf.getName()));

                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());

                for (InetAddress addr : addrs) {
                    // Log.v(TAG, "inside");

                    if (!addr.isLoopbackAddress()) {

                        boolean isIPv4 = addr instanceof Inet4Address;

                        if (isIPv4) {
                            // Log.v(TAG, "isnt loopback");
                            String sAddr = addr.getHostAddress().toUpperCase();
                            Log.v(TAG, "ip=" + sAddr);

                            if (sAddr.contains("192.168.49.")) {
                                Log.v(TAG, "ip = " + sAddr);
                                return sAddr;
                            }
                        }

                    }

                }
            }

        } catch (Exception ex) {
            Log.v(TAG, "error in parsing");
        } // for now eat exceptions
        Log.v(TAG, "returning empty ip address");
        return "";
    }

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null)
                    return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0)
                    buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
        /*
         * try { // this is so Linux hack return
         * loadFileAsString("/sys/class/net/" +interfaceName +
         * "/address").toUpperCase().trim(); } catch (IOException ex) { return
         * null; }
         */
    }




}