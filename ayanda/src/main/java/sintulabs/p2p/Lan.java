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

    private List<Device> deviceList;

    private Set<String> servicesDiscovered;

    private NearbyMedia fileToShare;
    private WebServer webServer;

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
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        try {
            localPort = findOpenSocket();
            if (webServer == null) {
                webServer = new WebServer(localPort);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // The name is   subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(SERVICE_NAME_DEFAULT);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(localPort);

        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);

        if (mRegistrationListener == null)
            initializeRegistrationListener();

        String msg;
        if (!serviceAnnounced) {
            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
            msg = "Announcing on LAN: " + SERVICE_NAME_DEFAULT + " : " + SERVICE_TYPE + "on port: " + String.valueOf(localPort);
        } else {
            msg = "Service already announced";
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

    private int findOpenSocket() throws java.io.IOException {
        // Initialize a server socket on the next available port.
        ServerSocket serverSocket = new ServerSocket(0);
        // Store the chosen port.
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
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
                            Device d = new Device(serviceInfo);
                            addDeviceToList(d);
                            connect(d);

                            updateDeviceList();
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
                removeDeviceFromList(new Device(service));
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

    private void addDeviceToList(Device device) {
        deviceList.add(device);
    }

    private void removeDeviceFromList(Device device) {
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

    /* Connect via HTTP & Download the File */
    public void connect(Device device) {
        // Build URL to connect to

        OkHttpClient client = new OkHttpClient();
        StringBuilder sbUrl = buildURLFromDevice(device).append(SERVICE_DOWNLOAD_FILE_PATH);
        Request request = buildRequest(sbUrl);
        try {
            Response response = client.newCall(request).execute();

            // Create Media Object
            NearbyMedia media = new NearbyMedia();
            media.mMimeType = response.header("Content-Type", "text/plain");
            media.mTitle = new Date().getTime() + "";
            setFileExtension(media);
            // Create File to store Media in
            File fileOut = createFile(media.mTitle);
            media.mFileMedia = fileOut;

            Neighbor neighbor = new Neighbor(device.getHost().getHostAddress(),
                    device.getHost().getHostName(), Neighbor.TYPE_WIFI_NSD);

            iLan.transferProgress(neighbor, fileOut, media.mTitle, media.mMimeType, 50,
                    Long.parseLong(response.header("Content-Length", "0")));


            BufferedSink sink = Okio.buffer(Okio.sink(fileOut));
            sink.writeAll(response.body().source());
            sink.close();

            //now get the metadata
            sbUrl = buildURLFromDevice(device).append(SERVICE_DOWNLOAD_METADATA_PATH);
            request = buildRequest(sbUrl);
            response = client.newCall(request).execute();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            sink = Okio.buffer(Okio.sink(baos));
            sink.writeAll(response.body().source());
            sink.close();

            media.mMetadataJson = new String(baos.toByteArray());

        } catch (IOException e) {
            Log.e(TAG_DEBUG, "Unable to connect to url: " + sbUrl.toString() + " ", e);
        }
    }

    /* Create a String representing the host and port of a device on LAN */
    private StringBuilder buildURLFromDevice(Device device) {
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


    public List<Device> getDeviceList() {
        return deviceList;
    }

    public static class Device {
        private InetAddress host;
        private Integer port;
        NsdServiceInfo serviceInfo;

        public Device(NsdServiceInfo serviceInfo) {
            this.port = serviceInfo.getPort();
            this.host = serviceInfo.getHost();
            this.serviceInfo = serviceInfo;
        }

        public InetAddress getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        public String getName() {
            return serviceInfo.getServiceName();
        }

    }

    /* Share file with nearby devices */
    public void shareFile(NearbyMedia media) throws IOException {
        this.fileToShare = media;

        announce();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer(int port) throws java.io.IOException {
            super(port);
            start();
        }


        @Override
        public Response serve(IHTTPSession session) {
            if (session.getUri().endsWith(SERVICE_DOWNLOAD_FILE_PATH)) {
                try {
                    return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, fileToShare.mMimeType, new FileInputStream(fileToShare.getFileMedia()));
                } catch (IOException ioe) {
                    return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", ioe.getLocalizedMessage());
                }
            } else if (session.getUri().endsWith(SERVICE_DOWNLOAD_METADATA_PATH)) {
                return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "text/plain", fileToShare.getMetadataJson());

            } else {
                String msg = "<html><body><h1>Hello server</h1>\n";
                Map<String, String> parms = session.getParms();
                if (parms.get("username") == null) {
                    msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
                } else {
                    msg += "<p>Hello, " + parms.get("username") + "!</p>";
                }
                return NanoHTTPD.newFixedLengthResponse(msg + "</body></html>\n");
            }
        }
    }
}
