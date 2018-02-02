package com.yjy.tnloader.TNLoader.Request;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCacheStrategy;
import com.yjy.tnloader.TNLoader.Engine.Engine;
import com.yjy.tnloader.TNLoader.Resource.EmptySignature;
import com.yjy.tnloader.TNLoader.Resource.Key;
import com.yjy.tnloader.TNLoader.Resource.RequestKey;
import com.yjy.tnloader.TNLoader.Resource.Resource;
import com.yjy.tnloader.TNLoader.Utils.Util;

import java.util.Queue;

/**
 * Created by software1 on 2018/1/31.
 */

public class GenericRequest<R> implements Request,SizeReadyCallback{


    private final String TAG = "GenericRequest";

    private int placeholderResourceId;
    private int errorResourceId;
    private float sizeMultiplier;
    private int overrideWidth;
    private int overrideHeight;
    private static final Queue<GenericRequest> REQUEST_POOL = Util.createQueue(0);
    private Status status;
    private Target target;
    private Drawable placeholderDrawable;

    private Engine engine;
    private boolean isMemoryCacheable;
    private DiskCacheStrategy diskCacheStrategy;
    private String url;
    private RequestKey key;
    private boolean hasKey = false;
    private Priority priority;
    private EmptySignature signature = EmptySignature.obtain();


    public static GenericRequest obtain(String url,Target target,int placeholderResourceId,Drawable placeholderDrawable,int errorResourceId,float sizeMultiplier,
                                        int overrideWidth,int overrideHeight,Engine engine,boolean isMemoryCacheable,
                                        DiskCacheStrategy diskCacheStrategy,Priority priority){
        GenericRequest request =  REQUEST_POOL.poll();
        if(request == null){
            request = new GenericRequest();
        }

        request.init(url,target,placeholderResourceId,placeholderDrawable,errorResourceId,sizeMultiplier,
        overrideWidth,overrideHeight,engine,isMemoryCacheable,diskCacheStrategy,priority);
        return request;

    }

    public Drawable getPlaceholderDrawable() {
        return placeholderDrawable;
    }


    private enum Status {
        /** Created but not yet running. */
        PENDING,
        /** In the process of fetching media. */
        RUNNING,
        /** Waiting for a callback given to the Target to be called to determine target dimensions. */
        WAITING_FOR_SIZE,
        /** Finished loading media successfully. */
        COMPLETE,
        /** Failed to load media, may be restarted. */
        FAILED,
        /** Cancelled by the user, may not be restarted. */
        CANCELLED,
        /** Cleared by the user with a placeholder set, may not be restarted. */
        CLEARED,
        /** Temporarily paused by the system, may be restarted. */
        PAUSED,
    }

    public void init(String url,Target target,int placeholderResourceId,Drawable placeholderDrawable,int errorResourceId,float sizeMultiplier,
                     int overrideWidth,int overrideHeight,Engine engine,boolean isMemoryCacheable,DiskCacheStrategy diskCacheStrategy,Priority priority){
        this.placeholderResourceId = placeholderResourceId;
        this.errorResourceId = errorResourceId;
        this.sizeMultiplier = sizeMultiplier;
        this.overrideHeight = overrideHeight;
        this.overrideWidth = overrideWidth;
        this.target = target;
        this.placeholderDrawable = placeholderDrawable;
        this.engine = engine;
        this.isMemoryCacheable = isMemoryCacheable;
        this.diskCacheStrategy = diskCacheStrategy;
        this.url = url;
        this.priority = priority;
        this.key = new RequestKey(url,signature,overrideWidth,overrideHeight);

        status = Status.PENDING;
    }



    @Override
    public RequestKey key() {
        return key;
    }

    @Override
    public void begin() {
        Log.e(TAG,"begin");

        status = Status.WAITING_FOR_SIZE;
        if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
            onSizeReady(overrideWidth, overrideHeight);
        } else {
            target.getSize(this);
        }

        if (!isComplete() && !isFailed()) {
            target.onLoadStarted(getPlaceholderDrawable());
        }

    }


    @Override
    public void onSizeReady(int width, int height) {
        if (status != Status.WAITING_FOR_SIZE) {
            return;
        }
        status = Status.RUNNING;
        width = Math.round(sizeMultiplier * width);
        height = Math.round(sizeMultiplier * height);
        //拦截器启动
        engine.load(width, height, diskCacheStrategy,this,priority);

    }

    @Override
    public void onResourceReady(Resource<?> resource) {
        if (resource == null) {
            return;
        }

        Object received = resource.get();

        onResourceReady(resource, (R) received);

    }

    @Override
    public DiskCacheStrategy getDiskStrategy() {
        return diskCacheStrategy;
    }

    @Override
    public boolean hasKeyResource() {
        return hasKey;
    }

    @Override
    public void setKeyResource(boolean hasKey) {
        this.hasKey = hasKey;
    }

    @Override
    public String url() {
        return url;
    }


    private void onResourceReady(Resource<?> resource, R result) {
        // We must call isFirstReadyResource before setting status.
//        boolean isFirstResource = isFirstReadyResource();
        status = Status.COMPLETE;
        target.onResourceReady(result, null);

    }




    @Override
    public void pause() {

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public boolean isResourceSet() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    @Override
    public boolean isMemoryCache() {
        return isMemoryCacheable;
    }

    @Override
    public void recycle() {
        this.target = null;
        this.placeholderDrawable = null;
        REQUEST_POOL.offer(this);
    }
}
