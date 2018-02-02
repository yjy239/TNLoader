package com.yjy.tnloader.TNLoader.Engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCache;
import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCacheStrategy;
import com.yjy.tnloader.TNLoader.Cache.MemoryCache;
import com.yjy.tnloader.TNLoader.Engine.interceptor.BitmapTransformInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.DecodeInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.DiskCacheInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.MemoryCacheInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.StreamInterceptor;
import com.yjy.tnloader.TNLoader.Engine.interceptor.RealInterceptorChain;
import com.yjy.tnloader.TNLoader.Request.Priority;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Resource.RequestKey;
import com.yjy.tnloader.TNLoader.Resource.RequestResource;
import com.yjy.tnloader.TNLoader.Resource.Key;
import com.yjy.tnloader.TNLoader.Resource.Resource;
import com.yjy.tnloader.TNLoader.Resource.ResourceRecycler;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by software1 on 2018/1/30.
 */

public class Engine implements MemoryCacheCallBack{
    private final String TAG="Engine";
    private Dispatcher dispatcher;
    private MemoryCache memoryCache;
    private DiskCache.Factory diskCacheFactory;
    private ExecutorService diskCacheService;
    private ExecutorService sourceService;
    private List<Interceptor> interceptors = new ArrayList<>();
    private final Map<Key, WeakReference<RequestResource<?>>> activeResources;
    private final int LOADFROMCACHE = 0;
    private ReferenceQueue<RequestResource<?>> resourceReferenceQueue;
    private ResourceRecycler recycler;
    private Context context;

    private volatile int working = 0;

    private Handler engineHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOADFROMCACHE:
                    break;

            }
        }
    };


    public Engine(Context context, Dispatcher dispatcher, MemoryCache memoryCache, DiskCache.Factory diskCacheFactory,
                  ExecutorService diskCacheService, ExecutorService sourceService) {
        this.dispatcher = dispatcher;
        this.memoryCache = memoryCache;
        this.diskCacheFactory = diskCacheFactory;
        this.diskCacheService = diskCacheService;
        this.sourceService = sourceService;
        this.context = context;
        activeResources = new HashMap<>();

        if(recycler == null){
            recycler = new ResourceRecycler();
        }
    }

    public void load(int width, int height, DiskCacheStrategy diskCacheStrategy, Request request, Priority priority) {

        int index = 0;
        DiskCache diskCache = diskCacheFactory.build();

        DecodeJob decodeJob = new DecodeJob(context,this,diskCache,diskCacheStrategy,width,height,request,interceptors,priority);
        sourceService.submit(decodeJob);

    }


    @Override
    public synchronized RequestResource<?> loadFromCache(Key key, boolean isMemoryCacheable) {
        //这里将等待其他线程处理好活跃缓存以及内存，并且等待的时候把资源让渡出去。
        try {
            while (working>0){
                wait();
            }
        }catch (InterruptedException e){
        }
        working++;

        if (!isMemoryCacheable) {
            working--;
            return null;
        }

        RequestResource<?> cached = getEngineResourceFromCache(key);
        if (cached != null) {
            cached.acquire();
            activeResources.put(key, new ResourceWeakReference(key, cached, getReferenceQueue()));
        }
        working--;
        return cached;
    }

    private RequestResource<?> getEngineResourceFromCache(Key key) {
        Resource<?> cached = memoryCache.remove(key);

        final RequestResource result;
        if (cached == null) {
            result = null;
        } else if (cached instanceof RequestResource) {
            // Save an object allocation if we've cached an RequestResource (the typical case).
            result = (RequestResource) cached;
        } else {
            result = new RequestResource(cached, true);
        }
        return result;
    }

    private ReferenceQueue<RequestResource<?>> getReferenceQueue() {
        if (resourceReferenceQueue == null) {
            resourceReferenceQueue = new ReferenceQueue<RequestResource<?>>();
            MessageQueue queue = Looper.myQueue();
            queue.addIdleHandler(new RefQueueIdleHandler(activeResources, resourceReferenceQueue));
        }
        return resourceReferenceQueue;
    }

    @Override
    public synchronized RequestResource<?> loadFromActiveResources(Key key, boolean isMemoryCacheable) {
        try {
            while (working>0){
                wait();
            }
        }catch (InterruptedException e){
        }
        working++;
        if (!isMemoryCacheable) {
            working--;
            return null;
        }

        RequestResource<?> active = null;
        WeakReference<RequestResource<?>> activeRef = activeResources.get(key);
        if (activeRef != null) {
            active = activeRef.get();
            if (active != null) {
                active.acquire();
            } else {
                activeResources.remove(key);
            }
        }
        working--;
        return active;
    }


    private static class ResourceWeakReference extends WeakReference<RequestResource<?>> {
        private final Key key;

        public ResourceWeakReference(Key key, RequestResource<?> r, ReferenceQueue<? super RequestResource<?>> q) {
            super(r, q);
            this.key = key;
        }
    }

    // Responsible for cleaning up the active resource map by remove weak references that have been cleared.
    private static class RefQueueIdleHandler implements MessageQueue.IdleHandler {
        private final Map<Key, WeakReference<RequestResource<?>>> activeResources;
        private final ReferenceQueue<RequestResource<?>> queue;

        public RefQueueIdleHandler(Map<Key, WeakReference<RequestResource<?>>> activeResources,
                                   ReferenceQueue<RequestResource<?>> queue) {
            this.activeResources = activeResources;
            this.queue = queue;
        }

        @Override
        public boolean queueIdle() {
            ResourceWeakReference ref = (ResourceWeakReference) queue.poll();
            if (ref != null) {
                activeResources.remove(ref.key);
            }

            return true;
        }
    }
}
