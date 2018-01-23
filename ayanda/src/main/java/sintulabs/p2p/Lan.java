package sintulabs.p2p;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sabzo on 12/26/17.
 */

public class Lan extends P2P{
    // constants for identifying service and service type
    public final static String SERVICE_NAME_DEFAULT = "NSDaya";
    public final static String SERVICE_TYPE = "_http._tcp.";
    public final static String LAN_DEVICE_NUM_UPDATE = "AYANDA_LAN_DEVICE_UPDATE";
    public final static String LAN_SERVICE_LOST = "AYANDA_LAN_SERVICE_LOST";
    // For discovery
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    // For announcing service
    private int mLocalPort;
    private Context mContext;
    private String mServiceName;
    private NsdManager.RegistrationListener mRegistrationListener;

    private NsdManager mNsdManager;
    private Boolean serviceAnnounced;

    private List<Device> deviceList;

    private Set<String> servicesDiscovered;

    private ILan iLan;

    public Lan(Context context, ILan iLan) {
        mContext = context;
        this.iLan = iLan;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        deviceList = new ArrayList<>();
        serviceAnnounced = false;
        servicesDiscovered = new HashSet<>();
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
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        int port = 0;
        try {
            port = findOpenSocket();
        } catch (IOException e) {
            e.printStackTrace();        }
        // The name is   subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(SERVICE_NAME_DEFAULT);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager)mContext.getSystemService(Context.NSD_SERVICE);

        if (mRegistrationListener == null)
            initializeRegistrationListener();

        String msg;
        if (!serviceAnnounced) {
            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
            msg = "Announcing on LAN: " + SERVICE_NAME_DEFAULT + " : " + SERVICE_TYPE + "on port: " + String.valueOf(port);
        } else {
            msg = "Service already announced";
        }

        Log.d(TAG_DEBUG, msg);
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
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
        mLocalPort =  serverSocket.getLocalPort();
        serverSocket.close();

        return mLocalPort;
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
                    // Service already discovered -- ignore it!
                }
                // Make sure service is the expect type and name
                else if ( service.getServiceType().equals(SERVICE_TYPE) &&
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
                            addDeviceToList(new Device(serviceInfo));
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
                Log.i(TAG_DEBUG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG_DEBUG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG_DEBUG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };

        //start discovery
        startDiscovery();
    }

    private void startDiscovery() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            mDiscoveryListener = null;
            servicesDiscovered.clear();
        }
    }

    public void stopAnnouncement() {
        if (mRegistrationListener != null) {
            mNsdManager.unregisterService(mRegistrationListener);
            mRegistrationListener = null;
        }
    }

    public void connect(InetAddress host, Integer port) {

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
}
