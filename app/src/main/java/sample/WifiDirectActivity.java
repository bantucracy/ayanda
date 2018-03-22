package sample;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import sintulabs.ayanda.R;
import sintulabs.p2p.Ayanda;
import sintulabs.p2p.Client;
import sintulabs.p2p.IWifiDirect;
import sintulabs.p2p.NearbyMedia;
import sintulabs.p2p.Server;

/**
 * Created by sabzo on 1/18/18.
 */

public class WifiDirectActivity extends AppCompatActivity {
    private ListView lvDevices;
    private List peers = new ArrayList();
    private List peerNames = new ArrayList();
    private ArrayAdapter<String> peersAdapter = null;

    private Button btnWdShareFile;
    private Button btnWdDiscover;

    NearbyMedia nearbyMedia;

    private Ayanda a;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
        setListeners();
        a = new Ayanda(this, null, null, new IWifiDirect() {
            @Override
            public void wifiP2pStateChangedAction(Intent intent) {

            }

            @Override
            public void wifiP2pPeersChangedAction() {
                peers.clear();
                // TODO fix error when WiFi off
                peers.addAll(a.wdGetDevicesDiscovered() );
                peerNames.clear();
                for (int i = 0; i < peers.size(); i++) {
                    WifiP2pDevice device = (WifiP2pDevice) peers.get(i);
                    peersAdapter.add(device.deviceName);
                }
            }

            @Override
            public void wifiP2pConnectionChangedAction(Intent intent) {

            }

            @Override
            public void wifiP2pThisDeviceChangedAction(Intent intent) {

            }

            @Override
            public void onConnectedAsServer(Server server) {
            
            }

            @Override
            public void onConnectedAsClient(final InetAddress groupOwnerAddress) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyClient client = new MyClient(WifiDirectActivity.this);
                        try {

                                final String response = client
                                    .get(groupOwnerAddress.getHostAddress() + ":" + Integer.toString(8080));
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(WifiDirectActivity.this, response, Toast.LENGTH_LONG).show();
                                }
                            });


                        } catch (IOException e) {
                           e.printStackTrace();
                        }

                        try {
                            final File file = client.getFile(groupOwnerAddress.getHostAddress() + ":" + Integer.toString(8080));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        if (nearbyMedia != null) {

                           client.uploadFile(groupOwnerAddress.getHostAddress() + ":" + Integer.toString(8080), nearbyMedia);
                        }
                    }
                }).start();

            }
        });
        try {
            int defualtPort = 8080;
            a.setServer(new MyServer(this, defualtPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createView() {
        setContentView(R.layout.wifidirect_activity);
        lvDevices = (ListView) findViewById(R.id.lvDevices);
        peersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNames);
        lvDevices.setAdapter(peersAdapter);
        btnWdShareFile = (Button) findViewById(R.id.btnWdShareFile);
        btnWdDiscover = (Button) findViewById(R.id.btnWdDiscover);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    private void setListeners() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnWdShareFile:
                        //p2p.announce();
                        onPickPhoto();
                        break;
                    case R.id.btnWdDiscover:
                        a.wdDiscover();
                        break;
                }
            }
        };
        AdapterView.OnItemClickListener deviceClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WifiP2pDevice device = (WifiP2pDevice) peers.get(i);
                a.wdConnect(device);
            }
        };
        btnWdShareFile.setOnClickListener(clickListener);
        btnWdDiscover.setOnClickListener(clickListener);
        lvDevices.setOnItemClickListener(deviceClick);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri photoUri = data.getData();
            // Do something with the photo based on Uri
            Bitmap selectedImage = null;
            // Load the selected image into a preview
            ImageView ivPreview = (ImageView) findViewById(R.id.ivPreviewWD);
            ivPreview.setImageBitmap(selectedImage);

            String filePath = getRealPathFromURI(photoUri);


            try {
                nearbyMedia = new NearbyMedia();
                nearbyMedia.setMimeType("image/jpeg");
                nearbyMedia.setTitle("pic");

                nearbyMedia.setFileMedia(new File(filePath));

                //get a JSON reprecation of the metadata we want to share
                Gson gson = new GsonBuilder()
                        .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
                nearbyMedia.mMetadataJson = gson.toJson("key:value");

                a.wdShareFile(nearbyMedia);
            } catch (IOException e) {
                nearbyMedia = null;
                e.printStackTrace();
            }

        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }
    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        a.wdRegisterReceivers();
    }

    /* unregister the broadcast receiver */

    @Override
    protected void onPause() {
        super.onPause();
        super.onPause();
        a.wdUnregisterReceivers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi_direct, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.miBt:
                startActivity(new Intent(this, BluetoothActivity.class ));
                finish();
                break;
            case R.id.miLan:
                startActivity(new Intent(this, LanActivity.class ));
                finish();
                break;
        }
        return true;
    }
}
