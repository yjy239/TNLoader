package com.yjy.tnloader;

import android.net.Uri;
import android.util.Log;

import com.yjy.tnloader.OkHttpSupport.FutureInputStream;
import com.yjy.tnloader.OkHttpSupport.RealInputStream;
import com.yjy.tnloader.TNLoader.Engine.RequestHandler.RequestHandler;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Request.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

/**
 * Created by software1 on 2018/2/7.
 */

public class OkhttpRequestHandler implements RequestHandler {

    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private InputStream stream;
    private static final int MAXIMUM_REDIRECTS = 5;
    private HttpURLConnection urlConnection;
    private volatile boolean isCancelled;

    private static final String TAG = "OkhttpRequestHandler";

    public OkhttpRequestHandler(){

    }


    @Override
    public boolean canHandleRequest(Request data) {
        //获取请求中的url,来自定义判断究竟是哪个数据流获取器来获取inputstream
        String scheme = Uri.parse(data.url()).getScheme();
        return (SCHEME_HTTP.equals(scheme) || SCHEME_HTTPS.equals(scheme));
    }

    @Override
    public Response load(final Request request) throws IOException {
        Log.e(TAG,"OKHTTP REQUESTHANDLER");
        //这里需要okhttp和tnloader两个线程之间沟通，需要一个小技巧，就是多线程设计的future模式
        RealInputStream real = new RealInputStream(request.url());
        FutureInputStream future = new FutureInputStream();
        future.setRealData(real);
        InputStream in = future.getInputStream();
        return new Response.Builder().request(request).setInputStream(in).build();
    }
}
