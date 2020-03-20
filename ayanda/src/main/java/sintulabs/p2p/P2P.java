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
    protected String deviceName;

    /**
     * Set the deviceName to be associated with this account
     * @param deviceName
     */
    protected void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * @return String deviceName
     */
    protected String getDeviceName() {
        return this.deviceName;
    }
    /* announce the p2p service */
    public abstract void announce();
    /* Discover a nearby Peer*/
    public abstract void discover();

    /* If connection method is supported */
    public abstract Boolean isSupported();
    /* If connection method is not only supported, but is available*/
    public abstract Boolean isEnabled();

}
