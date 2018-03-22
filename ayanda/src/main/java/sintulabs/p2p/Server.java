package sintulabs.p2p;


import java.io.IOException;

public class Server {

    public static Server server;
    private IServer mServer;

    private Server(IServer mServer) {
        this.mServer = mServer;
    }


    public static Server createInstance(IServer mServer) {
        server = (server == null) ? server = new Server(mServer) : server;
        return server;
    }

    public int getPort() {
        int port = 0;
        if (mServer != null) {
            port = mServer.getPort();
        }
        return port;
    }

    public void setFileToShare(NearbyMedia media) {
       mServer.setFileToShare(media);
    }

    public static Server getInstance() throws IOException {
        if (server == null) {
            throw new IOException("Server not defined");
        }
        return server;
    }

}