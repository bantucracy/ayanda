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
    public abstract void discover();
    // Connect to a Peer
    public abstract void connect();
    // Disconnect from a peer
    public abstract void disconnect();
    // Send file to peer
    public abstract void send();
    // Cancel file transfer in progress
    public abstract void cancel();
}
