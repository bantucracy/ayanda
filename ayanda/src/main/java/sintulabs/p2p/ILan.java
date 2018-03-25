package sintulabs.p2p;

import android.net.nsd.NsdServiceInfo;

import java.io.File;

/**
 * Created by sabzo on 1/21/18.
 */

public interface ILan {
    // Runs on UI thread
    public void deviceListChanged();
    public void transferComplete (Neighbor neighbor, NearbyMedia media);
    public void transferProgress (Neighbor neighbor, File fileMedia, String title, String mimeType,
                                  long transferred, long total);
    public void serviceRegistered(String serviceName);
    public void serviceResolved(NsdServiceInfo serviceInfo);
}
