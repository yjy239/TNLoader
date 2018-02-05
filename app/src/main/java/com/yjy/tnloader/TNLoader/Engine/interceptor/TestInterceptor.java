package com.yjy.tnloader.TNLoader.Engine.interceptor;

import android.util.Log;

import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

/**
 * Created by software1 on 2018/2/1.
 */

public class TestInterceptor implements Interceptor {
    private final String TAG = "Test";
    @Override
    public Response intercept(Chain chain) {
        Log.e(TAG,"自定义");
        Request request = chain.request();
        Response response = chain.proceed(request);
        return response;
    }
}
