package com.yjy.tnloader.TNLoader.Engine.interceptor;

import android.util.Log;

import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Engine.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

import java.io.IOException;

/**
 * Created by software1 on 2018/1/31.
 */

public class BitmapTransformInterceptor implements Interceptor {
    private final String TAG = "BitmapTransform";


    @Override
    public Response intercept(Chain chain){
        Log.e(TAG,"图片转化");
        Request request = chain.request();

        Response response = chain.proceed(request);


        return response;
    }
}
