package sintulabs.p2p;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;


public class Server {
    public static Server server;
    public static MServer mServer;
    private NearbyMedia fileToShare;
    public void setFileToShare(NearbyMedia file) {
        fileToShare = file;
    }

    private class MServer extends NanoHTTPD {



        public final static String SERVICE_DOWNLOAD_FILE_PATH = "/nearby/file";
        public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/nearby/meta";

        public MServer(int port) throws IOException {
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
    }

    private Server(int port) throws IOException {
        mServer = new MServer(port);
    }

    /**
     * Create a user defined server
     * @param userDefinedServer
     */
    public static void setInstance(Server userDefinedServer) {
        server = userDefinedServer;
    }


    public static Server getInstance(int port) throws IOException {
        server = (server == null) ? server = new Server(port) : server;
        return server;
    }
}