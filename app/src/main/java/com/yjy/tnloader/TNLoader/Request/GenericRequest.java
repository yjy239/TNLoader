package com.yjy.tnloader.TNLoader.Request;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCacheStrategy;
import com.yjy.tnloader.TNLoader.Engine.Engine;
import com.yjy.tnloader.TNLoader.Resource.DecodeFormat;
import com.yjy.tnloader.TNLoader.Resource.EmptySignature;
import com.yjy.tnloader.TNLoader.Resource.Key;
import com.yjy.tnloader.TNLoader.Resource.RequestKey;
import com.yjy.tnloader.TNLoader.Resource.Resource;
import com.yjy.tnloader.TNLoader.Utils.Util;

import java.io.InputStream;
import java.util.Queue;

/**
 * Created by software1 on 2018/1/31.
 */

public class GenericRequest<R> implements Request,SizeReadyCallback,ResourceCallback{

    private final String TAG = "GenericRequest";

    private int placeholderResourceId;
    private int errorResourceId;
    private float sizeMultiplier;
    private int overrideWidth = Integer.MAX_VALUE;
    private int overrideHeight = Integer.MAX_VALUE;
    private static final Queue<GenericRequest> REQUEST_POOL = Util.createQueue(0);
    private Status status;
    private Target target;
    private Drawable placeholderDrawable;
    private Drawable errorDrawable;

    private Engine engine;
    private boolean isMemoryCacheable;
    private DiskCacheStrategy diskCacheStrategy;
    private String url;
    private RequestKey key;
    private boolean hasKey = false;
    private Priority priority;
    private EmptySignature signature = EmptySignature.obtain();
    private DecodeFormat format = DecodeFormat.DEFAULT;
    private Resource<?> resource;
    private static final int MSG_COMPLETE = 1;
    private static final int MSG_EXCEPTION = 2;
    private Handler MAIN_HANDLER  = new Handler(Looper.getMainLooper(),new MainThreadCallback());
    private InputStream in;
    private Context mContext;




    public static GenericRequest obtain(Context mContext,String url,Target target,int placeholderResourceId,Drawable placeholderDrawable,int errorResourceId,Drawable errorDrawable,float sizeMultiplier,
                                        int overrideWidth,int overrideHeight,Engine engine,boolean isMemoryCacheable,
                                        DiskCacheStrategy diskCacheStrategy,Priority priority,DecodeFormat format){
        GenericRequest request =  REQUEST_POOL.poll();
        if(request == null){
            request = new GenericRequest();
        }

        request.init(mContext,url,target,placeholderResourceId,placeholderDrawable,errorResourceId,errorDrawable,sizeMultiplier,
        overrideWidth,overrideHeight,engine,isMemoryCacheable,diskCacheStrategy,priority,format);
        return request;

    }

    public Drawable getPlaceholderDrawable() {
        if (placeholderDrawable == null && placeholderResourceId > 0) {
            placeholderDrawable = mContext.getResources().getDrawable(placeholderResourceId);
        }
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

    public void init(Context mContext,String url,Target target,int placeholderResourceId,Drawable placeholderDrawable,int errorResourceId,Drawable errorDrawable,float sizeMultiplier,
                     int overrideWidth,int overrideHeight,Engine engine,boolean isMemoryCacheable,DiskCacheStrategy diskCacheStrategy,Priority priority,
                     DecodeFormat format){
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
        this.format = format;
        this.mContext = mContext;
        this.errorDrawable = errorDrawable;
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
        engine.load(width, height, diskCacheStrategy,this,priority,this);

    }

    @Override
    public void onResourceReady(Resource<?> resource) {

        MAIN_HANDLER.obtainMessage(MSG_COMPLETE,resource).sendToTarget();

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

    @Override
    public int getWidth() {
        return overrideWidth;
    }

    @Override
    public int getHeight() {
        return overrideHeight;
    }

    @Override
    public DecodeFormat getFormat() {
        return format;
    }

    @Override
    public Resource<?> resultResource() {
        return resource;
    }

    @Override
    public void setResource(Resource<?> resource) {
        this.resource = resource;
    }

    @Override
    public InputStream in() {
        return in;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void onException(Exception e) {
        MAIN_HANDLER.obtainMessage(MSG_EXCEPTION,resource).sendToTarget();
    }

    private void setErrorPlaceholder(Exception e) {

        Drawable error = getErrorDrawable();
//        if (error == null) {
//            error = getErrorDrawable();
//        }
        if (error == null) {
            error = getPlaceholderDrawable();
        }
        target.onLoadFailed(e, error);
    }

    private Drawable getErrorDrawable() {
        if (errorDrawable == null && errorResourceId > 0) {
            errorDrawable = mContext.getResources().getDrawable(errorResourceId);
        }
        return errorDrawable;
    }


    private void onResourceReady(Resource<?> resource, R result) {
        // We must call isFirstReadyResource before setting status.
//        boolean isFirstResource = isFirstReadyResource();
        status = Status.COMPLETE;
        target.onResourceReady(result, null);

    }


    @Override
    public void pause() {
        clear();
        status = Status.PAUSED;

    }

    @Override
    public void clear() {
        Util.assertMainThread();
        if (status == Status.CLEARED) {
            return;
        }
        cancel();
        // Resource must be released before canNotifyStatusChanged is called.
        if (resource != null) {
            releaseResource(resource);
        }

        target.onLoadCleared(getPlaceholderDrawable());

        // Must be after cancel().
        status = Status.CLEARED;

    }

    private void cancel() {
        status = Status.CANCELLED;
    }

    private void releaseResource(Resource<?> resource) {
        engine.release(resource);
        this.resource = null;
    }

    @Override
    public boolean isPaused() {
        return status == Status.PAUSED;
    }

    @Override
    public boolean isRunning() {
        return status == Status.RUNNING || status == Status.WAITING_FOR_SIZE;
    }

    @Override
    public boolean isComplete() {
        return status == Status.COMPLETE;
    }

    @Override
    public boolean isResourceSet() {
        return isComplete();
    }

    @Override
    public boolean isCancelled() {
        return status == Status.CANCELLED || status == Status.CLEARED;
    }

    @Override
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    @Override
    public boolean isMemoryCache() {
        return isMemoryCacheable;
    }


    private class MainThreadCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            if (MSG_COMPLETE == message.what || MSG_EXCEPTION == message.what) {
                if (MSG_COMPLETE == message.what) {
                    if(message.obj instanceof Resource){
                        Resource<?> resource = (Resource<?>) message.obj;
                        if (resource == null) {
                            return false;
                        }
                        Object received = resource.get();
                        onResourceReady(resource, (R) received);
                    }
                } else {
                    status = Status.FAILED;

                }
                return true;
            }

            return false;
        }
    }

    @Override
    public void recycle() {
        this.target = null;
        this.placeholderDrawable = null;
        if(in != null){
            try {
                in.close();
            }catch (Exception e){

            }
        }
        in = null;
        resource = null;
        isMemoryCacheable = false;
        key = null;

        REQUEST_POOL.offer(this);
    }
}
