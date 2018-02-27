package sintulabs.p2p;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.IntentSender;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

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
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private Boolean wiFiP2pEnabled = false;
    private ArrayList <WifiP2pDevice> peers = new ArrayList();
    private IWifiDirect iWifiDirect;

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
            // wifiP2pManager.requestConnectionInfo(wifiDirectChannel, connectionInfoListener);
        }
        iWifiDirect.wifiP2pConnectionChangedAction(intent);
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

            }
        });

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

    @Override
    public void announce() {}

    @Override
    public void discover() {
        discoverPeers();
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