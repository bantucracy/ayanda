package sample;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

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
        //    sAyanda = new Ayanda(this, iBluetooth, iLan, iWifiDirect);
            sAyanda = new Ayanda(this, null, iLan, null);
        }
        return sAyanda;
    }

      public synchronized void addMedia (final NearbyMedia nearbyMedia)
      {
          //use this to story received media

          //Read from: nearbyMedia.mUriMedia and write to permanent storage


      }


    public void initNearbyMedia () throws IOException
    {


        mNearbyMedia = new NearbyMedia();
        Uri uriMedia = Uri.parse("/path/to/your/file");
        mNearbyMedia.mUriMedia = uriMedia;

        InputStream is = getContentResolver().openInputStream(uriMedia);
        byte[] digest = getDigest(is);
        mNearbyMedia.mDigest = digest;

        String title = "My File";

        Gson gson = new GsonBuilder()
                .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
        mNearbyMedia.mMetadataJson = gson.toJson(mNearbyMedia);

        mNearbyMedia.mTitle = "My File";
        mNearbyMedia.mMimeType = "image/jpeg";
    }

}
