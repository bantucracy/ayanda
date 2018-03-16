package sintulabs.p2p;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;


public class Server extends NanoHTTPD {
    private NearbyMedia fileToShare;
    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/nearby/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/nearby/meta";
    public Server(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (fileToShare == null) {
            return newFixedLengthResponse(
                    Response.Status.NO_CONTENT, "text/plain", "No content found. Try again"
            );
        } else if(session.getUri().endsWith(SERVICE_DOWNLOAD_FILE_PATH)) {
            try {

                return newChunkedResponse(Response.Status.OK, fileToShare.getmMimeType(), new FileInputStream(fileToShare.mFileMedia));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("error sanding message", e.getLocalizedMessage() + ": " + e.getMessage());
                return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.getLocalizedMessage());
            }
        } else if(session.getUri().endsWith(SERVICE_DOWNLOAD_METADATA_PATH)) {
            return NanoHTTPD.newFixedLengthResponse(Response.Status.OK,"text/plain", fileToShare.mMetadataJson);
        } else {
            return newFixedLengthResponse(
                    Response.Status.NO_CONTENT, "text/plain", "unknown request"
            );
        }

    }

    public void setFileToShare(NearbyMedia file) {
        fileToShare = file;
    }
}