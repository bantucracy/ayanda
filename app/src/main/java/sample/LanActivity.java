package sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import sintulabs.ayanda.R;
import sintulabs.p2p.Ayanda;
import sintulabs.p2p.ILan;
import sintulabs.p2p.Lan;
import sintulabs.p2p.NearbyMedia;
import sintulabs.p2p.Neighbor;
import sintulabs.p2p.WifiDirect;

/**
 * Created by sabzo on 1/10/18.
 */

public class LanActivity extends AppCompatActivity {

    private WifiDirect p2p;
    private ListView lvDevices;
    private List peers = new ArrayList();
    private ArrayAdapter<String> peersAdapter = null;
    private List peerNames = new ArrayList();

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Buttons
    private Button btnLanAnnounce;
    private Button btnLanDiscover;
    // image
    private ImageView ivPreview;
    // LAN

    NearbyMedia nearbyMedia;
    private Ayanda a;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
        setListeners();
        a = new Ayanda(this, null, new ILan() {
            @Override
            public void deviceListChanged() {
                peers.clear();
                peerNames.clear();
                peersAdapter.clear();

                peers.addAll(a.lanGetDeviceList());
                for (int i = 0; i < peers.size(); i++) {
                    Ayanda.Device d = (Ayanda.Device) peers.get(i);
                    peersAdapter.add(d.getName());
                }
            }

            @Override
            public void transferComplete(Neighbor neighbor, NearbyMedia media) {

            }

            @Override
            public void transferProgress(Neighbor neighbor, File fileMedia, String title,
                                         String mimeType, long transferred, long total) {

            }

            @Override
            public void serviceRegistered(String serviceName) {
                Toast.makeText(LanActivity.this, "Successfully registered service: " + serviceName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void serviceResolved(NsdServiceInfo serviceInfo) {
                // Connected to desired service, so now make socket connection to peer
                final Ayanda.Device device = new Ayanda.Device(serviceInfo);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyClient client = new MyClient(LanActivity.this);
                        String host = device.getHost().toString();
                        try {

                            final String response = client
                                    .get(host + ":" + Integer.toString(8080));
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LanActivity.this, response, Toast.LENGTH_LONG).show();
                                }
                            });


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            final File file = client.getFile(host + ":" + Integer.toString(8080));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        if (nearbyMedia != null) {

                            client.uploadFile(host + ":" + Integer.toString(8080), nearbyMedia);
                        }
                    }
                }).start();

            }
        }, null);

       verifyStoragePermissions(this);
        try {
            a.setServer(new MyServer(this, Ayanda.findOpenSocket()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    private void createView() {
        setContentView(R.layout.activity_main);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // buttons
        btnLanAnnounce = (Button) findViewById(R.id.btnLanAnnounce);
        btnLanDiscover = (Button) findViewById(R.id.btnLanDiscover);
        // image
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        // ListView
        lvDevices = (ListView) findViewById(R.id.lvDevices);
        peersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNames);
        lvDevices.setAdapter(peersAdapter);
    }


    private void setListeners() {
        View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnLanAnnounce:
                            onPickPhoto();

                        break;
                    case R.id.btnLanDiscover:
                        a.lanDiscover();
                        break;
                }
            }
        };

        btnLanAnnounce.setOnClickListener(btnClick);
        btnLanDiscover.setOnClickListener(btnClick);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.miWd:
                startActivity(new Intent(this, WifiDirectActivity.class ));
                finish();
                break;
        }
        return true;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri photoUri = data.getData();
            // Do something with the photo based on Uri
            Bitmap selectedImage = null;
            // Load the selected image into a preview
            ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
            ivPreview.setImageBitmap(selectedImage);

            String filePath = getRealPathFromURI(photoUri);

            nearbyMedia = new NearbyMedia();
            nearbyMedia.setMimeType("image/jpeg");
            nearbyMedia.setTitle("pic");

            nearbyMedia.setFileMedia(new File(filePath));

            //get a JSON representation of the metadata we want to share
            Gson gson = new GsonBuilder()
                    .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            nearbyMedia.mMetadataJson = gson.toJson("key:value");
            try {
                a.lanShare(nearbyMedia);
            } catch (IOException e) {
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    protected void onStop() {
        super.onStop();
        a.lanStopAnnouncement();
        a.lanStopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}