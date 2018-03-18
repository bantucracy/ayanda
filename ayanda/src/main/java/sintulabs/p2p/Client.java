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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/ayanda/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/ayanda/meta";
    public final static String SERVICE_UPOAD_PATH = "/ayanda/upload";

    public static Client getInstance( Context applicationContext) {
        client = (client != null) ? client : new Client(applicationContext);
        return client;
    }

    /**
     * Set a custom user defined Client
     * @param userDefinedClient
     * @param applicationContext
     */
    public static void setInstance(Client userDefinedClient, Context applicationContext) {
        client = userDefinedClient;
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

    public static Boolean uploadFile(String serverURL, File file) {
        try {

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("some-field", "some-value")
                    .build();

            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(requestBody)
                    .build();

            mClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        // Handle the error
                    }
                }

            });

            return true;
        } catch (Exception ex) {
            // Handle the error
        }
        return false;
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
            String mimeType = response.header("Content-Type", "text/plain");

            String fileExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

            if (fileExt == null) {
                if (mimeType.startsWith("image"))
                    fileExt = "jpg";
                else if (mimeType.startsWith("video"))
                    fileExt = "mp4";
                else if (mimeType.startsWith("audio"))
                    fileExt = "m4a";
            }

            String title  = new Date().getTime() + "." + fileExt;

            File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            fileOut = new File(dirDownloads, title);

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
