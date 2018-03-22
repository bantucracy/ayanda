package sample;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import sintulabs.p2p.ILan;
import sintulabs.p2p.IServer;
import sintulabs.p2p.NearbyMedia;

/**
 * Created by sabzo on 3/22/18.
 */

public class MyServer extends NanoHTTPD implements IServer{

    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/nearby/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/nearby/meta";

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
        if (session.getUri().endsWith(SERVICE_DOWNLOAD_FILE_PATH)) {
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

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public void setFileToShare(NearbyMedia media) {
        this.fileToShare = media;
    }
}
