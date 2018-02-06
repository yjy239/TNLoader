package com.yjy.tnloader.TNLoader.Engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;
import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCache;
import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCacheStrategy;
import com.yjy.tnloader.TNLoader.Engine.RequestHandler.AssetRequestHandler;
import com.yjy.tnloader.TNLoader.Engine.RequestHandler.ContactsPhotoRequestHandler;
import com.yjy.tnloader.TNLoader.Engine.RequestHandler.NetworkRequestHandler;
import com.yjy.tnloader.TNLoader.Engine.RequestHandler.RequestHandler;
import com.yjy.tnloader.TNLoader.Engine.RequestHandler.ResourceRequestHandler;
import com.yjy.tnloader.TNLoader.Engine.executor.Prioritized;
import com.yjy.tnloader.TNLoader.Engine.interceptor.BitmapTransformInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.DecodeInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.DiskCacheInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.MemoryCacheInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.RealInterceptorChain;
import com.yjy.tnloader.TNLoader.Engine.interceptor.StreamInterceptor;
import com.yjy.tnloader.TNLoader.Request.Priority;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Request.ResourceCallback;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Resource.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yjy on 2018/1/30.
 * glide4.0中enginerunnable 和 DecodeJob的角色统一了
 */

public class DecodeJob  implements Runnable, Prioritized {

    private volatile boolean isCancelled = false;
    private final String TAG = "DecodeJob";
    private DiskCacheStrategy diskCacheStrategy;
    private DiskCache diskCache;
    private int width;
    private int height;
    private Request request;
    private int index = 0;
    private List<Interceptor> interceptors = new ArrayList<>();
    private List<RequestHandler> handlers = new ArrayList<>();
    private Engine engine;
    private Context context;
    private Priority priority;
    private BitmapPool bitmapPool;
    private ResourceCallback callback;
    private List<Interceptor> customInterceptor;
    private Interceptor customMemoryCache;
    private Interceptor customDiskCache;
    private Interceptor customNetWork;


    public DecodeJob(Context context, Engine engine, DiskCache diskCache, DiskCacheStrategy diskCacheStrategy,
                     int width, int height, Request request, List<Interceptor> interceptors, Priority priority, BitmapPool bitmapPool,
                     ResourceCallback callback,Interceptor customMemoryCache,
                     Interceptor customDiskCache,Interceptor customNetWork,List<Interceptor> customInterceptor,
                     List<RequestHandler> customHandler){
        this.diskCacheStrategy = diskCacheStrategy;
        this.diskCache = diskCache;
        this.width = width;
        this.height = height;
        this.request = request;
        this.interceptors = interceptors;
        this.engine = engine;
        this.context = context;
        this.priority = priority;
        this.bitmapPool = bitmapPool;
        this.callback = callback;
        this.customInterceptor = customInterceptor;
        this.customMemoryCache = customMemoryCache;
        this.customDiskCache = customDiskCache;
        this.customNetWork = customNetWork;
        if(customHandler!=null){
            handlers.addAll(customHandler);
        }
        handlers.add(new AssetRequestHandler(context));
        handlers.add(new ContactsPhotoRequestHandler(context));
        handlers.add(new NetworkRequestHandler());
        handlers.add(new ResourceRequestHandler());
    }

    public void cancel() {
        isCancelled = true;
    }

    @Override
    public void run() {

        if(isCancelled){
            return;
        }
        if(customInterceptor!=null){
            interceptors.addAll(customInterceptor);
        }
        if(customMemoryCache!=null){
            interceptors.add(customMemoryCache);
        }else {
            interceptors.add(new MemoryCacheInterceptor(engine,bitmapPool));
        }

        if(customDiskCache != null){
            interceptors.add(customDiskCache);
        }else {
            interceptors.add(new DiskCacheInterceptor(diskCache,bitmapPool));
        }

        if(customNetWork != null){
            interceptors.add(customNetWork);
        }else {
            interceptors.add(new StreamInterceptor(getHandler(request)));
        }

        interceptors.add(new DecodeInterceptor(bitmapPool));
        interceptors.add(new BitmapTransformInterceptor());

        Log.e(TAG,"拦截器启动");
        Response response = new Response.Builder().request(request).build();
        RealInterceptorChain chain = new RealInterceptorChain(interceptors,index,response);
        Response result = chain.proceed(request);

        Resource<?> resource = result.getResult();

        //repsonse就有了bitmap,这个时候，回调设置
        if (isCancelled) {
            if (resource != null) {
                resource.recycle();
            }
            return;
        }

        //
        if(result!= null){
            if(resource == null){
                onLoadFail(result.getException());
            }else {
                onLoadSuccess(resource);
            }
        }else {
            onLoadFail(null);
        }
        response.clear();
        result.clear();

    }

    private void onLoadSuccess(Resource<?> resource) {
        callback.onResourceReady(resource);
    }

    private void onLoadFail(Exception e) {
        Log.i(TAG,"无效地址");
        callback.onException(e);
    }

    private RequestHandler getHandler(Request request){
        RequestHandler handler = null;
        for (int i=0;i<handlers.size();i++){
            if(handlers.get(i).canHandleRequest(request)){
                handler = handlers.get(i);
            }
        }
        return handler;
    }

    @Override
    public int getPriority() {
        return priority.ordinal();
    }

}
