package sintulabs.p2p;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import static android.content.ContentValues.TAG;

/**
 * Client class
 */

public class Client {
    private static OkHttpClient mClient;
    public static Client client = null;
    private Context applicationContext;

    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/nearby/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/nearby/meta";

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

    public String get(String url) throws IOException {
        url = buildUrl(url, null);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    /**
     * Downloads file from specified url
     * @param url
     * @return File objet or null
     * @throws IOException
     */
    public File getFile(String url) throws IOException {
        url = buildUrl(url, SERVICE_DOWNLOAD_FILE_PATH);

        Request request = new Request.Builder().url(url).build();
        File fileOut = null;
        // Request file from server and store details
        try {
            Response response = mClient.newCall(request).execute();

            File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            fileOut = new File(dirDownloads, new Date().getTime() + "");

            InputStream inputStream = response.body().byteStream();

            BufferedSink sink = Okio.buffer(Okio.sink(fileOut));
            sink.writeAll(response.body().source());
            sink.close();


        } catch (IOException e) {
            Log.e(TAG, "Unable to connect to url: " + url, e);
        }

        return fileOut;
    }


    private String buildUrl(String url, String path) {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("http://");
        sbUrl.append(url);
        if (path != null && !path.isEmpty()) {
            sbUrl.append(path);
        }
        return sbUrl.toString();
    }
}
