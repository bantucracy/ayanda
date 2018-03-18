package sintulabs.p2p;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newChunkedResponse;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;


public class Server {
    public static Server server;
    public static MServer mServer;
    private NearbyMedia fileToShare;
    public void setFileToShare(NearbyMedia file) {
        fileToShare = file;
    }

    private class MServer extends NanoHTTPD {


        /* Create Routes */
        public final static String SERVICE_DOWNLOAD_FILE_PATH = "/ayanda/file";
        public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/ayanda/meta";
        public final static String SERVICE_UPOAD_PATH = "/ayanda/upload";

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
            }

            else if(session.getUri().endsWith(SERVICE_DOWNLOAD_FILE_PATH)) {
                return downloadFile();
            }

            else if(session.getUri().endsWith(SERVICE_DOWNLOAD_METADATA_PATH)) {
                return newFixedLengthResponse(Response.Status.OK,"text/plain", fileToShare.mMetadataJson);
            }

            else if (NanoHTTPD.Method.POST.equals(session.getMethod()) || Method.PUT.equals(session.getMethod())) {
              return uploadFile(session);
            }

            else {
                return newFixedLengthResponse(
                        Response.Status.NO_CONTENT, "text/plain", "unknown request"
                );
            }
        }
    }

    /**
     * Handle client download request
     * @return
     */
    private NanoHTTPD.Response downloadFile() {
        try {

            return newChunkedResponse(NanoHTTPD.Response.Status.OK, fileToShare.getmMimeType(), new FileInputStream(fileToShare.mFileMedia));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("error sending message", e.getLocalizedMessage() + ": " + e.getMessage());
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", e.getLocalizedMessage());
        }
    }

    /**
     * Handle file upload request
     * @return
     */

    private NanoHTTPD.Response uploadFile(NanoHTTPD.IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        Log.d("server","inside receive file!");
        try{
            session.parseBody(files);
            String title  = new Date().getTime() + ".";// + fileExt;

            File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File fileOut = new File(dirDownloads, title);

            File file = new File(files.get("uploadFile"));

            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK, "text/plain", "File successfully uploaded"
            );

        } catch (Exception e) {
            Log.d("server","error on parseBody" +e.toString());
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", e.getLocalizedMessage());
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