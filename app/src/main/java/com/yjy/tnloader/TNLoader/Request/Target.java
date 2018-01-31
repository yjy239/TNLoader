package com.yjy.tnloader.TNLoader.Request;

import android.graphics.drawable.Drawable;

import com.yjy.tnloader.TNLoader.manager.LifecycleListener;

/**
 * Created by software1 on 2018/1/30.
 */

public interface Target<R> extends LifecycleListener {
    /**
     * Indicates that we want the resource in its original unmodified width and/or height.
     */
    int SIZE_ORIGINAL = Integer.MIN_VALUE;

    /**
     * A lifecycle callback that is called when a load is started.
     *
     * <p>
     *     Note - This may not be called for every load, it is possible for example for loads to fail before the load
     *     starts (when the model object is null).
     * </p>
     *
     * <p>
     *     Note - This method may be called multiple times before any other lifecycle method is called. Loads can be
     *     paused and restarted due to lifecycle or connectivity events and each restart may cause a call here.
     * </p>
     *
     * @param placeholder The placeholder drawable to optionally show, or null.
     */
    void onLoadStarted(Drawable placeholder);

    /**
     * A lifecycle callback that is called when a load fails.
     *
     * <p>
     *     Note - This may be called before {@link #onLoadStarted(android.graphics.drawable.Drawable)} if the model
     *     object is null.
     * </p>
     *
     * @param e The exception causing the load to fail, or null if no exception occurred (usually because a decoder
     *          simply returned null).
     * @param errorDrawable The error drawable to optionally show, or null.
     */
    void onLoadFailed(Exception e, Drawable errorDrawable);

    /**
     * The method that will be called when the resource load has finished.
     *
     * @param resource the loaded resource.
     */
    void onResourceReady(R resource, Animation<? super R> animation);

    /**
     * A lifecycle callback that is called when a load is cancelled and its resources are freed.
     *
     * @param placeholder The placeholder drawable to optionally show, or null.
     */
    void onLoadCleared(Drawable placeholder);

    /**
     * A method to retrieve the size of this target.
     *
     * @param cb The callback that must be called when the size of the target has been determined
     */
    void getSize(SizeReadyCallback cb);

    /**
     * Sets the current request for this target to retain, should not be called outside of Glide.
     */
    void setRequest(Request request);

    /**
     * Retrieves the current request for this target, should not be called outside of Glide.
     */
    Request getRequest();
}
