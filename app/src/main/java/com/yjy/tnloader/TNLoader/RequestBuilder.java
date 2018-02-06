package com.yjy.tnloader.TNLoader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCacheStrategy;
import com.yjy.tnloader.TNLoader.Request.GenericRequest;
import com.yjy.tnloader.TNLoader.Request.ImageViewTarget;
import com.yjy.tnloader.TNLoader.Request.Priority;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Request.Target;
import com.yjy.tnloader.TNLoader.Resource.DecodeFormat;
import com.yjy.tnloader.TNLoader.manager.Lifecycle;
import com.yjy.tnloader.TNLoader.manager.RequestTracker;

/**
 * Created by software1 on 2018/1/31.
 */

public class RequestBuilder {

    private String mUrl;
    private Lifecycle mLifecycle;
    private RequestTracker requestTracker;
    private int placeholderResourceId;
    private int errorResourceId;
    private float sizeMultiplier;
    private Drawable errorPlaceholder;
    private Drawable placeholderDrawable;
    private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.RESULT;
    private boolean isMemoryCacheable;
    private TNLoader loader;
    private Priority priority = Priority.NORMAL;
    private DecodeFormat decodeFormat;
    private int overrideWidth = Integer.MAX_VALUE;
    private int overrideHeight = Integer.MAX_VALUE;
    private Context mContext;



    public RequestBuilder(Context mContext,TNLoader loader,String url, Lifecycle lifecycle, RequestTracker requestTracker){
        mUrl = url;
        this.mLifecycle = lifecycle;
        this.requestTracker= requestTracker;
        this.loader = loader;
        this.mContext = mContext;
    }

    public GenericRequest build(Target target){

        return GenericRequest.obtain(mContext,mUrl,target,placeholderResourceId,placeholderDrawable,errorResourceId,errorPlaceholder,sizeMultiplier,
                overrideWidth,overrideHeight,loader.getEngine(),isMemoryCacheable,diskCacheStrategy,priority,decodeFormat);
    }


    /**
     * Sets a resource to display if a load fails.
     *
     * @param resourceId The id of the resource to use as a placeholder.
     * @return This request builder.
     */
    public RequestBuilder error(
            int resourceId) {
        this.errorResourceId = resourceId;

        return this;
    }

    /**
     * Sets a {@link Drawable} to display if a load fails.
     *
     * @param drawable The drawable to display.
     * @return This request builder.
     */
    public RequestBuilder error(
            Drawable drawable) {
        this.errorPlaceholder = drawable;

        return this;
    }

    /**
     * Sets an Android resource id for a {@link android.graphics.drawable.Drawable} resourceto display while a resource
     * is loading.
     *
     * @param resourceId The id of the resource to use as a placeholder
     * @return This request builder.
     */
    public RequestBuilder placeholder(
            int resourceId) {
        this.placeholderResourceId = resourceId;

        return this;
    }

    /**
     * Sets an {@link android.graphics.drawable.Drawable} to display while a resource is loading.
     *
     * @param drawable The drawable to display as a placeholder.
     * @return This request builder.
     */
    public RequestBuilder placeholder(
            Drawable drawable) {
        this.placeholderDrawable = drawable;

        return this;
    }

    public RequestBuilder deformat(DecodeFormat format){
        this.decodeFormat = format;
        return this;
    }

    public RequestBuilder memoryCache(boolean isMemoryCacheable){
        this.isMemoryCacheable = isMemoryCacheable;
        return this;
    }

    public RequestBuilder sizeMultiplier(float sizeMultiplier){
        this.sizeMultiplier = sizeMultiplier;
        return this;
    }

    //图片获取线程优先度
    public RequestBuilder priority(Priority priority){
        this.priority = priority;
        return this;
    }

    public RequestBuilder diskCacheStrategy(DiskCacheStrategy diskCacheStrategy){
        this.diskCacheStrategy = diskCacheStrategy;
        return this;
    }

    public RequestBuilder width(int overrideWidth){
        this.overrideWidth = overrideWidth;
        return this;
    }

    public RequestBuilder height(int overrideHeight){
        this.overrideHeight = overrideHeight;
        return this;
    }


    public Target into(ImageView view){

        ImageViewTarget target = new ImageViewTarget(view);
        Request previous = target.getRequest();

        if (previous != null) {
            previous.clear();
            requestTracker.removeRequest(previous);
            previous.recycle();
        }

        Request request = build(target);
        target.setRequest(request);
        mLifecycle.addListener(target);
        requestTracker.runRequest(request);

        return target;
    }

}
