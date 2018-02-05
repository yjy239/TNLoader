package com.yjy.tnloader.TNLoader.Request;

/**
 * Created by software1 on 2018/1/30.
 */

import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCacheStrategy;
import com.yjy.tnloader.TNLoader.Resource.DecodeFormat;
import com.yjy.tnloader.TNLoader.Resource.RequestKey;
import com.yjy.tnloader.TNLoader.Resource.Resource;

import java.io.InputStream;

/**
 * A request that loads a resource for an {@link }.
 */
public interface Request {

    /***
     * key of request**/
    RequestKey key();

    /**is MemoryCache open*/
    boolean isMemoryCache();

    /**资源刷新*/
    void onResourceReady(Resource<?> resource);

    /***磁盘策略**/
    DiskCacheStrategy getDiskStrategy();

    /**是否存在key对应的磁盘缓存*/
    boolean hasKeyResource();


    void setKeyResource(boolean hasKey);

    String url();

    /**获取宽高**/
    int getWidth();
    int getHeight();

    /**获取解码**/
    DecodeFormat getFormat();

    /**获取转化后的资源*/
    Resource<?> resultResource();

    void setResource(Resource<?> resource);

    InputStream in();
    void setInputStream(InputStream in);



    /**
     * Starts an asynchronous load.
     */
    void begin();

    /**
     * Identical to {@link #clear()} except that the request may later be restarted.
     */
    void pause();

    /**
     * Prevents any bitmaps being loaded from previous requests, releases any resources held by this request,
     * displays the current placeholder if one was provided, and marks the request as having been cancelled.
     */
    void clear();

    /**
     * Returns true if this request is paused and may be restarted.
     */
    boolean isPaused();

    /**
     * Returns true if this request is running and has not completed or failed.
     */
    boolean isRunning();

    /**
     * Returns true if the request has completed successfully.
     */
    boolean isComplete();

    /**
     * Returns true if a non-placeholder resource is set. For Requests that load more than one resource, isResourceSet
     * may return true even if {@link #isComplete()}} returns false.
     */
    boolean isResourceSet();

    /**
     * Returns true if the request has been cancelled.
     */
    boolean isCancelled();

    /**
     * Returns true if the request has failed.
     */
    boolean isFailed();

    /**
     * Recycles the request object and releases its resources.
     */
    void recycle();
}
