package sample.custom;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import sintulabs.p2p.NearbyMedia;

import static android.content.ContentValues.TAG;

/**
 * Created by sabzo on 3/22/18.
 */

public class MyClient {
    private static OkHttpClient mClient;

    private Context applicationContext;

    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/ayanda/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/ayanda/meta";
    public final static String SERVICE_UPLOAD_FILE_PATH = "/ayanda/upload";

    /**
     * Create a Client object
     */
    public MyClient(Context applicationContext) {
        if (mClient == null) {
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

    public Boolean uploadFile(String url, final NearbyMedia file) {
        try {
            url = buildUrl(url, SERVICE_UPLOAD_FILE_PATH);



            final AssetFileDescriptor fd = applicationContext.getContentResolver().openAssetFileDescriptor(file.getMediaUri(), "r");
            if (fd == null) {
                throw new FileNotFoundException("could not open file descriptor");
            }
            RequestBody requestFile = new RequestBody() {
                @Override
                public long contentLength() { return fd.getDeclaredLength(); }
                @Override
                public MediaType contentType() { return MediaType.parse(file.getmMimeType()); }
                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    InputStream is = fd.createInputStream();
                    sink.writeAll(Okio.buffer(Okio.source(is)));
                }
            };

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getTitle(),
                            requestFile)
                    .addFormDataPart("fileExt", getFileExtension(file.getmMimeType()))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            mClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("", "Upload request unsuccessful");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d("", "Upload unsuccessful");// Handle the error
                    } else {
                        Log.d("", "Upload successful");
                    }
                }
            });

            return true;
        } catch (Exception ex) {
            Log.d("", "Error Uploading file. " + ex.getLocalizedMessage());
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

            String fileExt = getFileExtension(mimeType);

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

    private String getFileExtension(String mimeType) {
        String fileExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

        if (fileExt == null) {
            if (mimeType.startsWith("image"))
                fileExt = "jpg";
            else if (mimeType.startsWith("video"))
                fileExt = "mp4";
            else if (mimeType.startsWith("audio"))
                fileExt = "m4a";
        }

        return fileExt;
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
