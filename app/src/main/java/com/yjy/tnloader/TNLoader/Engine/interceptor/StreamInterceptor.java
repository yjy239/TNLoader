package com.yjy.tnloader.TNLoader.Engine.interceptor;

import android.util.Log;

import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Engine.RequestHandler.RequestHandler;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

/**
 * Created by yjy on 2018/1/31.
 */

public class StreamInterceptor implements Interceptor {
    private final String TAG = "NetWork";

    private RequestHandler requestHandler;
    public StreamInterceptor(RequestHandler requestHandler){
        this.requestHandler = requestHandler;
    }

    @Override
    public Response intercept(Chain chain)throws Exception {
        Log.e(TAG,"数据解析，获取流");
        Request request = chain.request();
        Response response = null;
        try {
            if(requestHandler != null){
               response = requestHandler.load(request);
            }else {
                Log.e(TAG,"没有处理对象");
            }
        }catch (Exception e){
            response = new Response.Builder().Exception(e).build();
            e.printStackTrace();
        }

        //只有获取到流才会进入到图片解码拦截器
        if(response.getInputStream() != null){
            request.setInputStream(response.getInputStream());
            response = chain.proceed(request);
        }

        return response;
    }
}
