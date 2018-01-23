package sintulabs.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;

import java.util.ArrayList;

/**
 * Created by sabzo on 1/19/18.
 */

public class Ayanda {
    private Bluetooth bt;
    private Lan lan;
    private WifiDirect wd;

    private Context context;

    public Ayanda(Context context, IWifiDirect iWifiDirect, ILan iLan, IBluetooth iBluetooth) {
        this.context = context;
        bt = new Bluetooth(context, iBluetooth);
        lan = new Lan(context, iLan);
        wd = new WifiDirect(context, iWifiDirect);
    }

    /*
        Discover nearby devices that have made themselves detectable blue Bluetooth
        Discovers nearby devices and stores them in a collection of devices found.
     */
    public void discover_by_bluetooth() {

    }
    /*
        Discover nearby devices using LAN:
        A device can register a service on the network and other devices connected on the network
        will be able to detect it.
     */
    public void discover_by_lan() {

    }


    /* Wifi Direct Methods */
    /*
        Discover nearby WiFi Direct enabled devices
     */
    public void wdDiscover() {
        wd.discover();
    }

    public void wdRegisterReceivers() {
        wd.registerReceivers();
    }

    public void wdUnregisterReceivers() {
        wd.unregisterReceivers();
    }

    public ArrayList<WifiP2pDevice> wdGetDevicesDiscovered() {
        return wd.getDevicesDiscovered();
    }
}
