package com.yjy.tnloader.TNLoader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.yjy.tnloader.TNLoader.Request.GenericRequest;
import com.yjy.tnloader.TNLoader.Request.ImageViewTarget;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Request.Target;
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
    private int overrideWidth;
    private int overrideHeight;
    private Drawable errorPlaceholder;
    private Drawable placeholderDrawable;


    public RequestBuilder(String url, Lifecycle lifecycle, RequestTracker requestTracker){
        mUrl = url;
        this.mLifecycle = lifecycle;
    }

    public GenericRequest build(){

        return GenericRequest.obtain(placeholderResourceId,errorResourceId,sizeMultiplier,
                overrideWidth,overrideHeight);
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

    public Target into(ImageView view){

        ImageViewTarget target = new ImageViewTarget(view);
        Request previous = target.getRequest();

        if (previous != null) {
            previous.clear();
            requestTracker.removeRequest(previous);
            previous.recycle();
        }

        Request request = build();
        target.setRequest(request);
        mLifecycle.addListener(target);
        requestTracker.runRequest(request);

        return target;
    }

}
