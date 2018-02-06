package sintulabs.p2p;

import java.io.File;

/**
 * Created by sabzo on 2/6/18.
 */

public class NearbyMedia {
    public String mTitle;
    public String mMimeType;
    public String mMetadataJson;
    public File mFileMedia;
    public byte[] mDigest;
    public long mLength;

    public void setFileMedia (File fileMedia)
    {
        mFileMedia = fileMedia;
        mDigest = Utils.getDigest(mFileMedia);
    }
}
