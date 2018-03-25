package sintulabs.p2p;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import fi.iki.elonen.NanoHTTPD;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by sabzo on 12/26/17.
 */

public class Lan extends P2P {
    // constants for identifying service and service type
    public final static String SERVICE_NAME_DEFAULT = "NSDaya";
    public final static String SERVICE_TYPE = "_http._tcp.";

    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/nearby/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/nearby/meta";


    // For discovery
    private NsdManager.DiscoveryListener mDiscoveryListener;
    // For announcing service
    private int localPort = 0;
    private Context mContext;
    private String mServiceName;
    private NsdManager.RegistrationListener mRegistrationListener;
    // for connecting
    private String clientID = ""; // This device's WiFi ID
    private NsdManager mNsdManager;
    private Boolean serviceAnnounced;
    private Boolean isDiscovering = false;

    private List<Ayanda.Device> deviceList;

    private Set<String> servicesDiscovered;

    private ILan iLan;

    public Lan(Context context, ILan iLan) {
        mContext = context;
        this.iLan = iLan;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        deviceList = new ArrayList<>();
        serviceAnnounced = false;
        servicesDiscovered = new HashSet<>();
        clientID = getWifiAddress(context);
    }

    public void setLocalPort(int port) {
        this.localPort = port;
    }

    @Override
    public Boolean isSupported() {
        return null;
    }

    @Override
    public Boolean isEnabled() {
        return null;
    }

    @Override
    public void announce() {
        String msg;
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        if (Server.server == null) {
            msg = "No Server implementation found";
            Log.d(TAG_DEBUG, msg);
        } else {
            // The name is   subject to change based on conflicts
            // with other services advertised on the same network.
            serviceInfo.setServiceName(SERVICE_NAME_DEFAULT);
            serviceInfo.setServiceType(SERVICE_TYPE);
            serviceInfo.setPort(localPort);

            mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);

            if (mRegistrationListener == null)
                initializeRegistrationListener();

            if (!serviceAnnounced) {
                mNsdManager.registerService(
                        serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
                msg = "Announcing on LAN: " + SERVICE_NAME_DEFAULT + " : " + SERVICE_TYPE + "on port: " + String.valueOf(localPort);
            } else {
                msg = "Service already announced";
            }
        }

        Log.d(TAG_DEBUG, msg);
    }


    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                serviceAnnounced = true;
                Log.d(TAG_DEBUG, "successfully registered service " + mServiceName);
                iLan.serviceRegistered(mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                Log.e(TAG_DEBUG, "Error registering service " + Integer.toString(errorCode));
                mRegistrationListener = null; // Allow service to be reinitialized
                serviceAnnounced = false; // Allow service to be re-announced
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                serviceAnnounced = false; // Allow service to be re-announced
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
            }
        };
    }

    /* Discover service */

    @Override
    public void discover() {
        /* TWO Steps:
            1. Create DiscoverListener
            2. Start Discovery and pass in DiscoverListener
         */

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG_DEBUG, "LAN Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG_DEBUG, "Service discovery success" + service);
                String hash = service.getServiceName();

                if (servicesDiscovered.contains(hash)) {
                    Log.d(TAG_DEBUG, "Service already discovered");
                    updateDeviceList();
                    // Service already discovered -- ignore it!
                }
                // Make sure service is the expect type and name
                else if (service.getServiceType().equals(SERVICE_TYPE) &&
                        service.getServiceName().contains(SERVICE_NAME_DEFAULT)) {

                    mNsdManager.resolveService(service, new NsdManager.ResolveListener() {

                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            // Called when the resolve fails.  Use the error code to debug.
                            Log.e(TAG_DEBUG, "Resolve failed" + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.e(TAG_DEBUG, "Resolve Succeeded. " + serviceInfo);
                            Ayanda.Device d = new Ayanda.Device(serviceInfo);
                            addDeviceToList(d);
                            updateDeviceList();
                            iLan.serviceResolved(serviceInfo);
                            Log.d(TAG_DEBUG, "Discovered Service: " + serviceInfo);
                        /* FYI; ServiceType within listener doesn't have a period at the end.
                         outside the listener it does */
                            servicesDiscovered.add(serviceInfo.getServiceName() + serviceInfo.getServiceType());
                        }
                    });
                }
                servicesDiscovered.add(hash);
            }


            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // remove service from list
                Log.e(TAG_DEBUG, "service lost" + service);
                removeDeviceFromList(new Ayanda.Device(service));
                servicesDiscovered.remove(service.getServiceName());
                updateDeviceList();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                isDiscovering = false;
                Log.i(TAG_DEBUG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                isDiscovering = false;
                Log.e(TAG_DEBUG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                isDiscovering = false;
                Log.e(TAG_DEBUG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };

        //start discovery
        startDiscovery();
    }

    /*  Update UI thread that device list has been changed */
    private void updateDeviceList() {
        // Runnable for main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                iLan.deviceListChanged();
            }
        });

    }

    private void addDeviceToList(Ayanda.Device device) {
        deviceList.add(device);
    }

    private void removeDeviceFromList(Ayanda.Device device) {
        int pos = -1; // pos of device to remove
        String deviceName = device.getName();
        String match;
        for (int i = 0; i < deviceList.size(); i++) {
            match = deviceList.get(i).getName();
            if (deviceName.contains(match)) {
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            deviceList.remove(pos);
        }
        updateDeviceList();
    }

    /**
     * Helper method to start discovery
     */
    private void startDiscovery() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        isDiscovering = true;
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            mDiscoveryListener = null;
            servicesDiscovered.clear();
            isDiscovering = false;
        }
    }

    public void stopAnnouncement() {
        if (mRegistrationListener != null) {
            mNsdManager.unregisterService(mRegistrationListener);
            mRegistrationListener = null;
        }
    }

    /* Create a String representing the host and port of a device on LAN */
    private StringBuilder buildURLFromDevice(Ayanda.Device device) {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("http://");
        sbUrl.append(device.getHost().getHostName());
        sbUrl.append(":").append(device.getPort());
        return sbUrl;
    }

    /* Create a Request Object */
    private Request buildRequest(StringBuilder url) {
        return new Request.Builder().url(url.toString())
                .addHeader("NearbyClientId", clientID).build();
    }

    private File createFile(String mTitle) {
        File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(dirDownloads, new Date().getTime() + "." + mTitle);
    }

    private void setFileExtension(NearbyMedia media) {
        String fileExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(media.mMimeType);

        if (fileExt == null) {
            if (media.mMimeType.startsWith("image"))
                fileExt = "jpg";
            else if (media.mMimeType.startsWith("video"))
                fileExt = "mp4";
            else if (media.mMimeType.startsWith("audio"))
                fileExt = "m4a";
        }
        media.mTitle += "." + fileExt;
    }

    /* Use WiFi Address as a unique device id */
    private String getWifiAddress(Context context) {
        Context applicationContext = context.getApplicationContext();
        WifiManager wifiManager = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return "noip";
        }
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        // handle IPv6!
        return String.format(Locale.ENGLISH,
                "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

    }


    public List<Ayanda.Device> getDeviceList() {
        return deviceList;
    }

    /* Share file with nearby devices */
    public void shareFile(NearbyMedia media) throws IOException {
        //this.fileToShare = media;
        Server.getInstance().setFileToShare(media);
        announce();
    }

}
