package com.example.coolweather.android.util;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
*网络请求
* */
public class HttpUtil {
    public static void sendOkHttpRequest(String address, Callback callback){
        Log.e("___",callback.toString());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
