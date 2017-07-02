package org.lamorim.huxflooderapp.api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by lucas on 22/11/2016.
 */

public class HuxAPIClient {
    private static final String BASE_URL = "https://www.thehuxley.com/api/v1/";
    private AsyncHttpClient client = new AsyncHttpClient();
    public HuxAPIClient() {

    }
    public HuxAPIClient(String token) {
        client.addHeader("Authorization", token);
    }
    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private  String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
