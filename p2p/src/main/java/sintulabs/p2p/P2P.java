package sintulabs.p2p;

/**
 * WiFi Direct P2P Class for detecting and connecting to nearby devices.
 * WifiDirect, Bluetooth inherit from this class and must implement a common interface
 * Created by sabzo on 12/20/17
 */
public abstract class P2P {
    protected final short  WIFIDIRECT = 0;
    protected final short BLUETOOTH = 1;
    protected final  String TAG_DEBUG = "ayanda_bug";

    // announce service
    protected abstract void announce();
    // Discover nearby Peer
    protected abstract void discover();
    // Connect to a Peer
    protected abstract void connect(String host, String port);
    // Disconnect from a peer
    protected abstract void disconnect();
    // Send file to peer
    protected abstract void send();
    // Cancel file transfer in progress
    protected abstract void cancel();
}
