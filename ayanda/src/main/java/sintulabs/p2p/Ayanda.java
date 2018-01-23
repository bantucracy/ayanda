package sintulabs.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;

import java.util.ArrayList;
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

    public Ayanda(Context context, IBluetooth iBluetooth, ILan iLan, IWifiDirect iWifiDirect) {
        this.context = context;
        bt = new Bluetooth(context, iBluetooth);
        lan = new Lan(context, iLan);
        wd = new WifiDirect(context, iWifiDirect);
    }

    /*
        Discover nearby devices that have made themselves detectable blue Bluetooth
        Discovers nearby devices and stores them in a collection of devices found.
     */
    public void btDiscover() {
        bt.discover();
    }

    public void btRegisterReceivers() {
        bt.registerReceivers();
    }

    public void btUnregisterReceivers() {
        bt.unregisterReceivers();
    }

    public void btAnnounce() {
        bt.announce();
    }

    public Set<String> btGetDeviceNamesDiscovered() {
        return bt.getDeviceNamesDiscovered();
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
    public List<Lan.Device> lanGetDeviceList() {
      return lan.getDeviceList();
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
