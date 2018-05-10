package sample;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import sintulabs.p2p.Ayanda;
import sintulabs.p2p.IBluetooth;
import sintulabs.p2p.ILan;
import sintulabs.p2p.IWifiDirect;
import sintulabs.p2p.NearbyMedia;
import sintulabs.p2p.impl.AyandaActivity;


public class AyandaSampleActivity extends AyandaActivity {


    private static Ayanda sAyanda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        startAyanda();
    }

    @Override
    public synchronized Ayanda getAyandaInstance(IBluetooth iBluetooth, ILan iLan, IWifiDirect iWifiDirect) {

        if (sAyanda == null) {
            sAyanda = new Ayanda(this, iBluetooth, iLan, iWifiDirect);
        //    sAyanda = new Ayanda(this, iBluetooth, null, null);
        }
        return sAyanda;
    }

      public synchronized void addMedia (final NearbyMedia nearbyMedia)
      {
          //use this to store received media

          //Read from: nearbyMedia.mUriMedia and write to permanent storage

          Toast.makeText(this,"Received file: " + nearbyMedia.getTitle(),Toast.LENGTH_SHORT).show();
      }


    public void initNearbyMedia () throws IOException
    {

        mNearbyMedia = new NearbyMedia();
        Uri uriMedia = Uri.parse("file:///android_asset/test.jpg");
        mNearbyMedia.mUriMedia = uriMedia;

        InputStream is = getAssets().open("test.jpg");
        byte[] digest = getDigest(is);
        mNearbyMedia.mDigest = digest;
        mNearbyMedia.mTitle = "Turtle of Akumal";
        mNearbyMedia.mMimeType = "image/jpeg";
        mNearbyMedia.mLength = getAssets().openFd("test.jpg").getLength();

        Gson gson = new GsonBuilder()
                .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
        mNearbyMedia.mMetadataJson = gson.toJson(mNearbyMedia);

    }

}
