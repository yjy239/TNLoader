package com.yjy.tnloader.TNLoader.Engine.interceptor;


import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;
import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Engine.MemoryCacheCallBack;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Resource.BitmapDrawableResource;
import com.yjy.tnloader.TNLoader.Resource.BitmapResource;
import com.yjy.tnloader.TNLoader.Resource.RequestResource;
import com.yjy.tnloader.TNLoader.Resource.Resource;

/**
 * Created by yjy on 2018/1/31.
 */

public class MemoryCacheInterceptor implements Interceptor {
    private final String TAG = "MemoryCache";

    private MemoryCacheCallBack engine;
    private BitmapPool bitmapPool;

    public MemoryCacheInterceptor(MemoryCacheCallBack callBack,BitmapPool bitmapPool){
        this.engine = callBack;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Response intercept(Chain chain)  {
        Log.e(TAG,"内存缓存");
        Response r=null;
        Request request = chain.request();
        //判断缓存是否存在
        RequestResource<?> cached = engine.loadFromCache(request.key(),request.isMemoryCache());
        if(cached != null){
            request.onResourceReady(cached);
        }


        //获取活跃的资源是否存在
        RequestResource<?> active = engine.loadFromActiveResources(request.key(),request.isMemoryCache());
        if (active != null) {
            request.onResourceReady(active);
        }
        //两者不存在进入下一个拦截器，并且加入到缓存中
        if(cached == null && active == null){
            r = chain.proceed(request);

            Resource<?> resource = r.getResult();
            if(resource instanceof BitmapResource){
                BitmapResource bitmapResource = (BitmapResource) resource;
                BitmapDrawable drawable = new BitmapDrawable(bitmapResource.get());
                BitmapDrawableResource drawableResource = new BitmapDrawableResource(drawable,bitmapPool);
                request.setResource(drawableResource);
            }
        }


        //组成response返回
//       r = new Response.Builder().request(request).build();
        return r;
    }
}
