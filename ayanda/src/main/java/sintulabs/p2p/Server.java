package sintulabs.p2p;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;


public class Server extends NanoHTTPD {
    private NearbyMedia fileToShare;
    public Server(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            if (fileToShare == null) {
                return newFixedLengthResponse(
                        Response.Status.NO_CONTENT, "text/plain", "No content found. Try again"
                );
            }
            return newChunkedResponse(Response.Status.OK, fileToShare.getmMimeType(), new FileInputStream(fileToShare.mFileMedia));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.getLocalizedMessage());
        }
    }

    public void setFileToShare(NearbyMedia file) {
        fileToShare = file;
    }
}