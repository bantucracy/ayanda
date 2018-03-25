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
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_DISABLED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED;

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
    private ArrayList <WifiP2pDevice> peers = new ArrayList();
    private IWifiDirect iWifiDirect;

    private NearbyMedia fileToShare;

    private int  serverPort = 8080;
    private Boolean isClient = false;
    private Boolean isServer = false;

    /**
     * Creates a WifiDirect instance
     * @param context activity/application contex
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

    public void setServerport(int port) {
        this.serverPort = port;
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
     *  Create WifiP2pManager and Channel
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
                    peers.clear();
                    peers.addAll(peerList.getDeviceList());
                }
            });
        }
        iWifiDirect.wifiP2pPeersChangedAction();
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
            // TODO Find group owner port
            wifiP2pManager.requestConnectionInfo(wifiDirectChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                    if (wifiP2pInfo.groupFormed) {
                        isGroupOwner = wifiP2pInfo.isGroupOwner;
                        groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
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
        iWifiDirect.onConnectedAsServer(Server.server);
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
    public ArrayList<WifiP2pDevice> getDevicesDiscovered() {
        return peers;
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
     * Set the file to share
     * @param fileToShare
     */
    public void setFileToShare(NearbyMedia fileToShare) {
       this.fileToShare = fileToShare;
    }

    public void shareFile(NearbyMedia file) {
        try {
            Server.getInstance().setFileToShare(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        discover();
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
}