package com.yjy.tnloader.TNLoader.Request;

/**
 * Created by yjy on 2018/2/1.
 */

import com.yjy.tnloader.TNLoader.Resource.Resource;

/**
 * A callback that listens for when a resource load completes successfully or fails due to an exception.
 */
public interface ResourceCallback {

    /**
     * Called when a resource is successfully loaded.
     *
     * @param resource The loaded resource.
     */
    void onResourceReady(Resource<?> resource);

    /**
     * Called when a resource fails to load successfully.
     *
     * @param e The exception that caused the failure, or null it the load failed for some reason other than an
     *          exception.
     */
    void onException(Exception e);
}
