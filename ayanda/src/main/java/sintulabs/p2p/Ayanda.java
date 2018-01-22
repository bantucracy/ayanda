package sintulabs.p2p;

import android.content.Context;

/**
 * Created by sabzo on 1/19/18.
 */

public class Ayanda {
    private Bluetooth bt;
    private Lan lan;
    private WifiDirect wd;
    private WifiDirectBroadcastReceiver wdr;

    private Context context;

    public Ayanda(Context context, IWifiDirect iWifiDirect, ILan iLan, IBluetooth iBluetooth) {
        this.context = context;
        bt = new Bluetooth(context);
        lan = new Lan(context);
        wd = new WifiDirect(context, iWifiDirect);
        //wdr = new WifiDirectBroadcastReceiver()
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
    /*
        Discover nearby WiFi Direct enabled devices
     */
    public void discover_by_wd() {

    }
}
