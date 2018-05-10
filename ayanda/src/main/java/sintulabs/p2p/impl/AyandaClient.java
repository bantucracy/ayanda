package sintulabs.p2p.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import sintulabs.p2p.Client;
import sintulabs.p2p.NearbyMedia;
import sintulabs.p2p.Utils;

import static android.content.ContentValues.TAG;

/**
 * Created by sabzo on 3/22/18.
 */

public class AyandaClient {
    private static OkHttpClient mClient;
    public static Client client = null;
    private Context applicationContext;

    public final static String SERVICE_DOWNLOAD_FILE_PATH = "/ayanda/file";
    public final static String SERVICE_DOWNLOAD_METADATA_PATH = "/ayanda/meta";
    public final static String SERVICE_UPLOAD_FILE_PATH = "/ayanda/upload";

    /**
     * Create a Client object
     */
    public AyandaClient(Context applicationContext) {
        if (client == null) {
            mClient = new OkHttpClient();
        }
        this.applicationContext = applicationContext;
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

    public Boolean uploadFile(String url, final NearbyMedia file, ProgressUIListener progressUIListener) {
        try {
            url = buildUrl(url, SERVICE_UPLOAD_FILE_PATH);

            final String mimeType = file.getmMimeType();

            RequestBody requestFile = new RequestBody() {
                @Override
                public long contentLength() { return file.mLength; }
                @Override
                public MediaType contentType() { return MediaType.parse(mimeType); }
                @Override
                public void writeTo(BufferedSink sink) throws IOException {

                    InputStream is = null;

                    if (file.getMediaUri().getPath().contains("android_asset"))
                    {
                        is = applicationContext.getAssets().open(file.getMediaUri().getLastPathSegment());
                    }
                    else
                        is = applicationContext.getContentResolver().openInputStream(file.getMediaUri());

                    sink.writeAll(Okio.buffer(Okio.source(is)));
                }
            };

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getTitle(),requestFile)
                    .addFormDataPart("fileExt", getFileExtension(file.getmMimeType()))
                    .build();

            //wrap your original request body with progress
            RequestBody requestBodyProgress = ProgressHelper.withProgress(requestBody, progressUIListener);

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBodyProgress)
                    .build();

            mClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("", "Upload request unsuccessful");
                    Log.e(TAG, "failed", e);
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
     * @param baseUrl
     * @return File objet or null
     * @throws IOException
     */
    public void getNearbyMedia(final String baseUrl, final ProgressUIListener progressUIListener, final AyandaListener nearbyListener) throws IOException {


//request builder
        Request.Builder builder = new Request.Builder();
        builder.url(buildUrl(baseUrl, SERVICE_DOWNLOAD_FILE_PATH));
        builder.get();

        //call
        Call call = mClient.newCall(builder.build());
//enqueue
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "=============onFailure===============");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("TAG", "=============onResponse===============");
                Log.e("TAG", "request headers:" + response.request().headers());
                Log.e("TAG", "response headers:" + response.headers());

                //your original response body
                ResponseBody body = response.body();
                //wrap the original response body with progress
                ResponseBody responseBody = ProgressHelper.withProgress(body, progressUIListener);

                NearbyMedia nearbyMedia = null;


                File fileOut = null;
                // Request file from server and store details
                try {

                    nearbyMedia = new NearbyMedia();

                    nearbyMedia.mMimeType = response.header("Content-Type", "text/plain");

                    String fileExt = getFileExtension(nearbyMedia.mMimeType);

                    String fileName = "oa-" + new Date().getTime() + '.' + fileExt;
                    nearbyMedia.mTitle  = fileName;
                    File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    fileOut = new File(dirDownloads, fileName);

                    BufferedSink sink = Okio.buffer(Okio.sink(fileOut));
                    sink.writeAll(response.body().source());
                    sink.close();

                    nearbyMedia.mUriMedia = Uri.fromFile(fileOut);
                    nearbyMedia.mLength = fileOut.length();
                    nearbyMedia.mDigest = Utils.getDigest(fileOut);

                    Request request = new Request.Builder().url( buildUrl(baseUrl, SERVICE_DOWNLOAD_METADATA_PATH)).build();
                    response = mClient.newCall(request).execute();
                    nearbyMedia.mMetadataJson = response.body().string();

                    if (nearbyListener != null)
                        nearbyListener.nearbyReceived(nearbyMedia);

                } catch (IOException e) {
                    Log.w(TAG, "Unable to connect to url: " + baseUrl, e);

                }
            }
        });


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
