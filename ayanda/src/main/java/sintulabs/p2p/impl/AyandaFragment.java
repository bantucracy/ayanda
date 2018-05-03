package sintulabs.p2p.impl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import io.github.lizhangqu.coreprogress.ProgressUIListener;
import sintulabs.p2p.Ayanda;
import sintulabs.p2p.IBluetooth;
import sintulabs.p2p.ILan;
import sintulabs.p2p.IWifiDirect;
import sintulabs.p2p.NearbyMedia;
import sintulabs.p2p.Neighbor;
import sintulabs.p2p.R;
import sintulabs.p2p.Server;


public abstract class AyandaFragment extends Fragment implements Runnable {

    private final static String TAG = "Nearby";

    private LinearLayout mViewNearbyDevices;

    protected NearbyMedia mNearbyMedia = null;

    private Ayanda mAyanda;
    private AyandaServer mAyandaServer;
    private HashMap<String,Ayanda.Device> mPeers = new HashMap<String,Ayanda.Device>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby, null);
    }

    @Override
    public void onAttach(Context context) {
        init();
        super.onAttach(context);
    }

    private void init ()
    {
        if (mViewNearbyDevices == null) {
            mViewNearbyDevices = getView().findViewById(R.id.nearbydevices);

            askForPermission("android.permission.BLUETOOTH", 1);
            askForPermission("android.permission.BLUETOOTH_ADMIN", 2);
            askForPermission("android.permission.ACCESS_FINE_LOCATION", 3);
            askForPermission("android.permission.ACCESS_WIFI_STATE", 4);
            askForPermission("android.permission.CHANGE_WIFI_STATE", 5);
            askForPermission("android.permission.ACCESS_NETWORK_STATE", 6);
            askForPermission("android.permission.CHANGE_NETWORK_STATE", 7);

            new Thread(this).start();
        }
    }

    public void run ()
    {
        mHandlerViews.sendEmptyMessage(1);

    }

    public abstract Ayanda getAyandaInstance (IBluetooth bt, ILan lan, IWifiDirect wifi);

    private void startAyanda ()
    {

        mAyanda = getAyandaInstance( mNearbyBluetooth, mNearbyWifiLan, mNearbyWifiDirect);
        mAyanda.wdRegisterReceivers();
        mAyanda.btRegisterReceivers();
        restartNearby();

        mNearbyWifiDirect.wifiP2pPeersChangedAction();
        mNearbyWifiLan.deviceListChanged();

        try {
            initNearbyMedia();
        }
        catch (Exception e)
        {}

        if (mNearbyMedia != null) {
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

        }


    }

    private boolean askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
            }

            return true;
        }

        return false;

    }


    public abstract void addMedia (final NearbyMedia nearbyMedia);

    protected void restartNearby() {
        mAyanda.lanDiscover();
        mAyanda.btDiscover();
    }

    protected void cancelNearby() {

        if (mAyandaServer != null)
            mAyandaServer.stop();

        mAyanda.lanStopAnnouncement();
        mAyanda.lanStopDiscovery();

        //stop wifi p2p?
    }

    private void log(String msg) {
        Log.d("Nearby", msg);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAyanda != null) {
            mAyanda.wdRegisterReceivers();
            mAyanda.btRegisterReceivers();
            restartNearby();
        }
    }

    /* unregister the broadcast receiver */
    @Override
    public void onPause() {
        super.onPause();

        if (mAyanda != null) {
            mAyanda.wdUnregisterReceivers();
            mAyanda.btUnregisterReceivers();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cancelNearby();
    }

    public abstract void initNearbyMedia () throws IOException;

    private void startServer() throws IOException {

        try {
            int defaultPort = 8080;
            mAyandaServer = new AyandaServer(getActivity(), defaultPort);
            mAyanda.setServer(mAyandaServer);

            mAyanda.wdShareFile(mNearbyMedia);
            mAyanda.lanShare(mNearbyMedia);
            mAyanda.btAnnounce();


        } catch (IOException e) {
            Log.e(TAG,"error setting server and sharing file",e);
        }


    }

    private void addPeerToView(Ayanda.Device newPeer) {

        mHandlerViews.sendEmptyMessage(0);
    }

    Handler mHandlerViews = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 0)
                refreshPeerViews();
            else if (msg.what == 1)
                startAyanda();
        }
    };


    private HashMap<String,DonutProgress> mDeviceToProgress = new HashMap();

    private void refreshPeerViews () {

        ArrayList<View> views = new ArrayList<>();

        Collection<Ayanda.Device> devices = new ArrayList<>(mPeers.values());

        for (final Ayanda.Device device : devices) {

            if (!TextUtils.isEmpty(device.getName())) {

                LinearLayout layoutOuter  =new LinearLayout(getActivity());
                layoutOuter.setLayoutParams(new LinearLayout.LayoutParams(220, 240));
                layoutOuter.setOrientation(LinearLayout.VERTICAL);
                layoutOuter.setPadding(5,5,5,5);
                layoutOuter.setGravity(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams imParams2 =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                DonutProgress donutProgress = mDeviceToProgress.get(device.getName()+device.getHost());
                if (donutProgress == null) {
                    donutProgress = new DonutProgress(getActivity());
                    mDeviceToProgress.put(device.getName()+device.getHost(),donutProgress);
                }

                donutProgress.setLayoutParams(imParams2);
                if (((LinearLayout)donutProgress.getParent()) != null)
                     ((LinearLayout)donutProgress.getParent()).removeView(donutProgress);

                layoutOuter.addView(donutProgress);

                LinearLayout.LayoutParams imParams3 =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                TextView tv = new TextView(getActivity());

                String deviceName = device.getName();
                deviceName = deviceName.replace("Ayanda.","");
                if (deviceName.length() > 15)
                    deviceName = deviceName.substring(0,12) + "...";

                tv.setText(deviceName);
             //   tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setLayoutParams(imParams3);
                layoutOuter.addView(tv);


                layoutOuter.setOnClickListener(new DeviceOnClickListener(device,donutProgress));

                views.add(layoutOuter);
            }
        }

        if (views.size() > 0)
            getView().findViewById(R.id.txt_tap_info).setVisibility(View.VISIBLE);
        else
            getView().findViewById(R.id.txt_tap_info).setVisibility(View.GONE);

        populateViews(mViewNearbyDevices, views.toArray(new View[views.size()]), getActivity());

    }

    class DeviceOnClickListener implements View.OnClickListener
    {
        private Ayanda.Device mDevice;
        private DonutProgress mProgress;

        public DeviceOnClickListener (Ayanda.Device device, DonutProgress progress) {
            mDevice = device;
            mProgress = progress;
        }

        @Override
        public void onClick(View view) {

            mProgress.setInnerBackgroundColor(Color.GREEN);

            if (mDevice.getType() == Ayanda.Device.TYPE_WIFI_P2P)
                mAyanda.wdConnect(mDevice);
            else if (mDevice.getType() == Ayanda.Device.TYPE_WIFI_LAN)
                connectToDevice(mDevice);
        }
    }

    ILan mNearbyWifiLan = new ILan() {

        @Override
        public String getPublicName () {
            return getLocalBluetoothName();
        }

        @Override
        public void deviceListChanged() {

            ArrayList<Ayanda.Device> devices = new ArrayList(mAyanda.lanGetDeviceList());

            for (Ayanda.Device device: devices)
            {
                //check that it has a name, is not us, and hasn't already been seen
                if ((!TextUtils.isEmpty(device.getName()))
                        && (!device.getName().equals(getPublicName()))
                        && (!mPeers.containsKey(device.getHost().toString()))) {


                    mPeers.put(device.getName(), device);
                    addPeerToView(device);
                }
            }

        }

        @Override
        public void transferComplete(Neighbor neighbor, NearbyMedia nearbyMedia) {

        }

        @Override
        public void transferProgress(Neighbor neighbor, File file, String s, String s1, long l, long l1) {


        }

        @Override
        public void serviceRegistered(String s) {

        }

        @Override
        public void serviceResolved(NsdServiceInfo serviceInfo) {

            // Connected to desired service, so now make socket connection to peer
            final Ayanda.Device device = new Ayanda.Device(serviceInfo);
            addPeerToView(device);


        }


    };

    public void connectToDevice (Ayanda.Device device)
    {
        String serverHost = null; //device.getHost().getHostName() + ":" + 8080;

        try {

            InetAddress hostInet =InetAddress.getByName(device.getHost().getHostAddress());

            if (!hostInet.isLoopbackAddress()) {

                byte [] addressBytes = hostInet.getAddress();

                // Inet6Address dest6 = Inet6Address.getByAddress(Data.get(position).getHost().GetHostAddress(), addressBytes, NetworkInterface.getByInetAddress(hostInet));
                InetAddress dest4 = Inet4Address.getByAddress (device.getHost().getHostAddress(), addressBytes);

                if (dest4 instanceof Inet6Address)
                    serverHost = "[" + dest4.getHostAddress() + "]:" + device.getPort().intValue();
                else
                    serverHost = dest4.getHostAddress() + ":" + device.getPort().intValue();

                getNearbyMedia(device,serverHost);
            }

        } catch (IOException e) {
            Log.e(TAG,"error LAN get: " + e);
            return;
        }
    }

    private void getNearbyMedia (final Ayanda.Device device, final String serverHost)
    {
        new Thread(new Runnable() {
            @Override public void run() {
                AyandaClient client = new AyandaClient(getActivity());

                try {

                    //if sharing a file, then do an upload
                    if (mNearbyMedia != null)
                    {
                        client.uploadFile(serverHost,mNearbyMedia, new ProgressUIListener() {

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressStart(long totalBytes) {
                                super.onUIProgressStart(totalBytes);
                                Log.d("TAG", "onUIProgressStart:" + totalBytes);
                            }

                            @Override
                            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {

                                if (device != null) {
                                    DonutProgress progress = mDeviceToProgress.get(device.getName() + device.getHost());
                                    if (progress != null)
                                        progress.setProgress((int) (100 * percent));
                                }
                            }

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressFinish() {
                                super.onUIProgressFinish();
                                Log.d("TAG", "onUIProgressFinish:");

                                if (device != null) {
                                    DonutProgress progress = mDeviceToProgress.get(device.getName() + device.getHost());
                                    if (progress != null)
                                        progress.setProgress(100);
                                }
                            }

                        });
                    }
                    else
                    {
                        //otherwise, do a download

                        client.getNearbyMedia(serverHost, new ProgressUIListener() {

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressStart(long totalBytes) {
                                super.onUIProgressStart(totalBytes);
                                Log.d("TAG", "onUIProgressStart:" + totalBytes);
                            }

                            @Override
                            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {

                                if (device != null) {
                                    DonutProgress progress = mDeviceToProgress.get(device.getName() + device.getHost());
                                    if (progress != null)
                                        progress.setProgress((int) (100 * percent));
                                }
                            }

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressFinish() {
                                super.onUIProgressFinish();
                                Log.d("TAG", "onUIProgressFinish:");
                                //  Toast.makeText(getApplicationContext(), "结束上传", Toast.LENGTH_SHORT).show();

                                if (device != null) {
                                    DonutProgress progress = mDeviceToProgress.get(device.getName() + device.getHost());
                                    if (progress != null)
                                        progress.setProgress(100);
                                }
                            }

                        }, new AyandaListener() {
                            @Override
                            public void nearbyReceived(NearbyMedia nearbyMedia) {

                                if (nearbyMedia != null && nearbyMedia.mUriMedia != null)
                                    addMedia(nearbyMedia);
                            }
                        });

                    }
                } catch (IOException e) {
                    Log.e(TAG,"error LAN get: " + e);
                }

            }

        }).start();

    }

     IBluetooth mNearbyBluetooth = new IBluetooth() {

         private boolean mInTransfer = false;
         private NearbyMedia mBtNearby = null;

         @Override
         public void actionDiscoveryStarted(Intent intent) {

         }

         @Override
         public void actionDiscoveryFinished(Intent intent) {

         }

         @Override
         public void stateChanged(Intent intent) {

         }

         @Override
         public void scanModeChange(Intent intent) {

         }

         @Override
         public void actionFound(Intent intent) {


             HashMap<String,BluetoothDevice> devices = new HashMap<>(mAyanda.btGetDevices());

             for (BluetoothDevice device: devices.values()) {
                 if ((!TextUtils.isEmpty(device.getAddress()))
                         && (!mPeers.containsKey(device.getAddress()))) {

                     Ayanda.Device aDevice = new Ayanda.Device(device);
                     mPeers.put(device.getName(), aDevice);
                     addPeerToView(aDevice);
                 }
             }


         }

         @Override
         public void dataRead(byte[] bytes, int numRead) {

         }

         @Override
         public void connected(BluetoothDevice device) {

         }

         @Override
         public String getPublicName() {
             return getLocalBluetoothName();
         }
     };

    IWifiDirect mNearbyWifiDirect = new IWifiDirect() {

        @Override
        public String getPublicName () {
            return getLocalBluetoothName();
        }

        @Override
        public void onConnectedAsClient(final InetAddress groupOwnerAddress) {


            AyandaClient client = new AyandaClient(getActivity());
            int defaultPort = 8080;

            Ayanda.Device device = mPeers.get(groupOwnerAddress.getHostAddress());
            String serverHost = groupOwnerAddress.getHostAddress() + ":" + Integer.toString(defaultPort);

            getNearbyMedia(device, serverHost);

        }

        @Override
        public void wifiP2pStateChangedAction(Intent intent) {

            Log.d(TAG, "wifiP2pStateChangedAction: " + intent.getAction() + ": " + intent.getData());

            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi P2P is enabled
                    mAyanda.wdDiscover();
                    mNearbyWifiDirect.wifiP2pPeersChangedAction();

                } else {
                    // Wi-Fi P2P is not enabled
                }
            }


        }

        @Override
        public void wifiP2pPeersChangedAction() {

            ArrayList<Ayanda.Device> devices = new ArrayList<>(mAyanda.wdGetDevicesDiscovered());

            for (Ayanda.Device device: devices)
            {
                if ((!TextUtils.isEmpty(device.getName()))
                        && (!device.getName().equals(getPublicName()))
                        && (!mPeers.containsKey(device.getName()))) {

                    mPeers.put(device.getName(), device);
                    addPeerToView(device);

                }


            }
        }

        @Override
        public void wifiP2pConnectionChangedAction(Intent intent) {
            Log.d(TAG, "wifiP2pConnectionChangedAction: " + intent.getAction() + ": " + intent.getData());

        }

        @Override
        public void wifiP2pThisDeviceChangedAction(Intent intent) {
            Log.d(TAG, "wifiP2pThisDeviceChangedAction: " + intent.getAction() + ": " + intent.getData());

        }

        @Override
        public void onConnectedAsServer(Server server) {

            //what to do here?
        }


    };

    private BluetoothAdapter mBluetoothAdapter;

    private String getLocalBluetoothName(){
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        String name = mBluetoothAdapter.getName();
        if(name == null){
            System.out.println("Name is null!");
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }

    /**
     * Copyright 2011 Sherif
     * Updated by Karim Varela to handle LinearLayouts with other views on either side.
     * @param linearLayout
     * @param views : The views to wrap within LinearLayout
     * @param context
     * @author Karim Varela
     **/
    private void populateViews(LinearLayout linearLayout, View[] views, Context context)
    {

        // kv : May need to replace 'getSherlockActivity()' with 'this' or 'getActivity()'
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        linearLayout.removeAllViews();
        int maxWidth = display.getWidth() - 20;

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params;
        LinearLayout newLL = new LinearLayout(context);
        newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        newLL.setGravity(Gravity.LEFT);
        newLL.setOrientation(LinearLayout.HORIZONTAL);

        int widthSoFar = 0;

        for (int i = 0; i < views.length; i++)
        {
            LinearLayout LL = new LinearLayout(context);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            LL.setLayoutParams(new ListView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            views[i].measure(0, 0);
            params = new LinearLayout.LayoutParams(views[i].getMeasuredWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);

            LL.addView(views[i], params);
            LL.measure(0, 0);
            widthSoFar += views[i].getMeasuredWidth();
            if (widthSoFar >= maxWidth)
            {
                linearLayout.addView(newLL);

                newLL = new LinearLayout(context);
                newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                newLL.setOrientation(LinearLayout.HORIZONTAL);
                newLL.setGravity(Gravity.CENTER_HORIZONTAL);
                params = new LinearLayout.LayoutParams(LL.getMeasuredWidth(), LL.getMeasuredHeight());
                newLL.addView(LL, params);
                widthSoFar = LL.getMeasuredWidth();
            }
            else
            {
                newLL.addView(LL);
            }
        }
        linearLayout.addView(newLL);
    }

    public static byte[] getDigest(InputStream is) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }


        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] sum = digest.digest();
            return sum;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for " + "SHA1", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing input stream", e);
            }
        }
    }
}
