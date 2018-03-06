package sintulabs.p2p;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Client class
 */

public class Client {
    private static OkHttpClient mClient;
    public static Client client = null;
    private Context applicationContext;

    public static Client getInstance( Context applicationContext) {
        client = (client != null) ? client : new Client(applicationContext);
        return client;
    }

    /**
     * Create a Client object
     */
    public Client(Context applicationContext) {
        if (client == null) {
            mClient = new OkHttpClient();
        }
    }

    public OkHttpClient getHTTPClient() {
        return mClient;
    }

    public String run(String url) throws IOException {
        url = buildUrl(url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    private String buildUrl(String url) {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("http://");
        sbUrl.append(url);
        return sbUrl.toString();
    }
}
