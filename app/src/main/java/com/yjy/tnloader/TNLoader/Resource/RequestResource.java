package com.yjy.tnloader.TNLoader.Resource;

/**
 * Created by software1 on 2018/2/1.
 */

import android.os.Looper;

/**
 * A wrapper resource that allows reference counting a wrapped {@link Resource}
 * interface.
 *
 * @param <Z> The type of data returned by the wrapped {@link Resource}.
 */
public class RequestResource<Z> implements Resource<Z> {
    private final Resource<Z> resource;
    private final boolean isCacheable;
    private ResourceListener listener;
    private Key key;
    private int acquired;
    private boolean isRecycled;

    public interface ResourceListener {
        void onResourceReleased(Key key, RequestResource<?> resource);
    }

    public RequestResource(Resource<Z> toWrap, boolean isCacheable) {
        if (toWrap == null) {
            throw new NullPointerException("Wrapped resource must not be null");
        }
        resource = toWrap;
        this.isCacheable = isCacheable;
    }

    public void setResourceListener(Key key, ResourceListener listener) {
        this.key = key;
        this.listener = listener;
    }

    public boolean isCacheable() {
        return isCacheable;
    }

    @Override
    public Z get() {
        return resource.get();
    }

    @Override
    public int getSize() {
        return resource.getSize();
    }

    @Override
    public void recycle() {
        if (acquired > 0) {
            throw new IllegalStateException("Cannot recycle a resource while it is still acquired");
        }
        if (isRecycled) {
            throw new IllegalStateException("Cannot recycle a resource that has already been recycled");
        }
        isRecycled = true;
        resource.recycle();
    }

    /**
     * Increments the number of consumers using the wrapped resource. Must be called on the main thread.
     *
     * <p>
     *     This must be called with a number corresponding to the number of new consumers each time new consumers
     *     begin using the wrapped resource. It is always safer to call acquire more often than necessary. Generally
     *     external users should never call this method, the framework will take care of this for you.
     * </p>
     */
    public void acquire() {
        if (isRecycled) {
            throw new IllegalStateException("Cannot acquire a recycled resource");
        }
        ++acquired;
    }

    /**
     * Decrements the number of consumers using the wrapped resource. Must be called on the main thread.
     *
     * <p>
     *     This must only be called when a consumer that called the {@link #acquire()} method is now done with the
     *     resource. Generally external users should never callthis method, the framework will take care of this for
     *     you.
     * </p>
     */
    public void release() {
        if (acquired <= 0) {
            throw new IllegalStateException("Cannot release a recycled or not yet acquired resource");
        }
        if (--acquired == 0) {
            listener.onResourceReleased(key, this);
        }
    }
}

