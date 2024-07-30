package com.reactnativeadmobile.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetUtils {
    private final String TAG = "NetUtils";
    private static NetUtils netUtils;
    public static final String BASE_URL = "https://api.shuashuati.com";
    private NetUtils(){}
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static NetUtils getNetUtils() {
        if(netUtils == null) {
            synchronized (NetUtils.class) {
                if(netUtils == null){
                    netUtils = new NetUtils();
                }
            }
        }
        return netUtils;
    }

    public interface ResultCallback{
        void success(String result);
        void failed();
    }

    public void request(String url,String json, ResultCallback callback) {
        RequestBody requestBody = RequestBody.create(JSON, json);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .cookieJar(CookieJar.NO_COOKIES)
                .callTimeout(10000, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("stateless", "true")
                .addHeader("authc", "eyJhbGciOiJIUzUxMiJ9.eyJkVHlwZSI6NCwiYnVzaW5lc3NJZCI6NzA3OTkwLCJtb2JpbGUiOiIxODgxMDA1NzM1MCIsInN1YkFjY3REYXRhU2NvcGUiOm51bGwsInNlc3Npb25UaW1lIjoyNTkyMDAwMDAwLCJzbiI6IjE4ODEwMDU3MzUwNDI5OTA3MiIsInVzZXJOYW1lIjoic3Fx5Luj55CG5ZWGIiwiZXhwIjoxNjg1MDcwMjE0LCJ1c2VySWQiOjcwNzk5MCwibG9naW5Qb3J0IjoiQVBJIn0.npkz7Xm8UtoBKS9lw5b4pjH3GALbehTGUHiSyCOH4Xc4TUvN-vi1QJuJm62oaXnV0btqt87XtHHk9W2M05H35w")
                .addHeader("clientId", "98a8f")
                .url(BASE_URL+url)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("onFailure",e.getMessage());
                callback.failed();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()) {
                    callback.failed();
                    //请求失败
                }
//                String result = Objects.requireNonNull(response.body()).string();
//                Log.d(TAG, "onResponse " + response.body().string());
//                ResultInfo resultInfo = JsonUtil.parseJson(response.body().string(), ResultInfo.class);
//                Log.d(TAG, "onResponse: ----" + resultInfo.getDataInfo().getTotal());
                callback.success(response.body().string());
            }
        });
    }
}
