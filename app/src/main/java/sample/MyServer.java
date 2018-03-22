package sample;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import sintulabs.p2p.ILan;
import sintulabs.p2p.IServer;
import sintulabs.p2p.NearbyMedia;

/**
 * Created by sabzo on 3/22/18.
 */

public class MyServer extends NanoHTTPD implements IServer{

    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/ayanda/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/ayanda/meta";
    public final static String SERVICE_UPLOAD_FILE_PATH = "/ayanda/upload";

    private NearbyMedia fileToShare;

    private int port;
    private Context context;

    public MyServer(Context context, int port) throws java.io.IOException {
        super(port);
        this.context = context;
        this.port = port;
        start();
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getUri().endsWith(SERVICE_UPLOAD_FILE_PATH)) {
            Response response = uploadFile(session);
            if (response != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "File received", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return response;
        } else if (session.getUri().endsWith(SERVICE_DOWNLOAD_FILE_PATH)) {
            try {

                Response response = NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, fileToShare.mMimeType, new FileInputStream(fileToShare.getFileMedia()));
                if (response != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "File sent", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return response;

            } catch (IOException ioe) {
                return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", ioe.getLocalizedMessage());
            }
        } else if (session.getUri().endsWith(SERVICE_DOWNLOAD_METADATA_PATH)) {
            return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "text/plain", fileToShare.getMetadataJson());

        } else {
            String msg = "<html><body><h1>Hello server</h1>\n";
            Map<String, String> parms = session.getParms();
            if (parms.get("username") == null) {
                msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
            } else {
                msg += "<p>Hello, " + parms.get("username") + "!</p>";
            }
            return NanoHTTPD.newFixedLengthResponse(msg + "</body></html>\n");
        }
    }

    private NanoHTTPD.Response uploadFile(NanoHTTPD.IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        Log.d("server","inside receive file!");
        try{
            String fileExt = "jpg";
            session.parseBody(files);
            String title  = new Date().getTime() + "." + fileExt;

            File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String filename = files.get("file");

            // Read file from temp directory
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(filename);
            byte[] b = new byte[(int)file.length()];
            fis.read(b);

            // write file to external storage
            file = new File(dirDownloads, title);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);

            /*
            RandomAccessFile f = new RandomAccessFile(filename, "r");
            byte[] b = new byte[(int)f.length()];
            f.readFully(b);
            */

            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK, "text/plain", "File successfully uploaded"
            );

        } catch (Exception e) {
            Log.d("server","error on parseBody" +e.toString());
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", e.getLocalizedMessage());
        }
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public void setFileToShare(NearbyMedia media) {
        this.fileToShare = media;
    }
}
