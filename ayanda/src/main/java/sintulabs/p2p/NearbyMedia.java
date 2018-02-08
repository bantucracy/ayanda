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

    public void setFileMedia (File fileMedia) {
        mFileMedia = fileMedia;
        mDigest = Utils.getDigest(mFileMedia);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getmMimeType() {
        return mMimeType;
    }

    public String getMetadataJson() {
        return mMetadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.mMetadataJson = metadataJson;
    }

    public File getFileMedia() {
        return mFileMedia;
    }

    public byte[] getDigest() {
        return mDigest;
    }

    public void setDigest(byte[] digest) {
        this.mDigest = digest;
    }

    public long getLength() {
        return mLength;
    }

    public void setLength(long fileMediaLength) {
        this.mLength = fileMediaLength;
    }

    public void setMimeType(String mimeType) {
        this.mMimeType = mimeType;
    }

    public void setTitle (String title) {
        mTitle = title;
    }
}
