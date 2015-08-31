package com.trcolgrove.contours.contoursGame;

/**/

import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * Class encapsulating an asynchronous post request to the server
 */
public class AsyncHttpRequest extends AsyncTask<NameValuePair, Integer, Integer> {

    private HttpCallback onSuccess = null;
    private HttpCallback onError = null;
    private HttpCallback onComplete = null;

    @IntDef({GET, POST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HttpMethod{}
    public static final int GET = 0;
    public static final int POST = 1;

    private String uri;
    private int method;
    private String TAG = "AsyncPost";

    public interface HttpCallback {
        void onFinished(int statusCode);
    }

    public AsyncHttpRequest(String uri, @HttpMethod int method) {
        this.uri = uri;
        this.method = method;
    }

    @Override
    protected Integer doInBackground(NameValuePair... params) {
        return makeRequest(params);
    }

    private Integer makeRequest(NameValuePair... params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpUriRequest httpRequest;

        switch(method) {
            case AsyncHttpRequest.GET:
                String req_uri = uri += URLEncodedUtils.format(Arrays.asList(params), "utf-8");
                httpRequest = new HttpGet(req_uri);
                break;
            case AsyncHttpRequest.POST:
                try {
                    httpRequest = new HttpPost(uri);
                    ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                    return -1;
                }
                break;
            default:
                httpRequest = new HttpGet();
        }

        HttpResponse response;
        try {
            response = httpClient.execute((httpRequest));
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return -1;
        }
        return response.getStatusLine().getStatusCode();
    }

    public void setOnSuccess(HttpCallback callback) {
        onSuccess = callback;
    }

    public void setOnError(HttpCallback callback) {
        onError = callback;
    }

    public void setOnComplete(HttpCallback callback) {
        onComplete = callback;
    }

    @Override
    public void onPostExecute(Integer statusCode) {
        if(statusCode < 300 && statusCode >= 200 && onSuccess != null) {
            onSuccess.onFinished(statusCode);
        } else if (statusCode >= 400 && onError != null) {
            onError.onFinished(statusCode);
        }

        if(onComplete != null) {
            onComplete.onFinished(statusCode);
        }
    }
}
