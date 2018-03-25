package sintulabs.p2p;



/**
 * Client class
 */

public class Client {
    private IClient mClient;
    public static Client client = null;

    /**
     * Create a Client object
     */
    private Client(IClient client) {
        this.mClient = client;
    }

    public static Client createInstance(IClient iclient) {
        return client = (client != null) ? client : new Client(iclient);
    }

    public static Client getInstance() {
        return client;
    }
}
