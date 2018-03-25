package sintulabs.p2p;


public class Neighbor {

    public String mId;
    public String mName;
    public int mType;

    public final static int TYPE_BLUETOOTH = 1;
    public final static int TYPE_WIFI_NSD = 2;
    public final static int TYPE_WIFI_P2P = 3;

    public Neighbor (String id, String name, int type) {
        mId = id;
        mName = name;
        mType = type;
    }
}
