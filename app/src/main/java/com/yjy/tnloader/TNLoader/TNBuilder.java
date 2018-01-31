package com.yjy.tnloader.TNLoader;

import android.content.Context;
import android.os.Build;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;
import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPoolAdapter;
import com.yjy.tnloader.TNLoader.Cache.BitmapPool.LruBitmapPool;
import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCache;
import com.yjy.tnloader.TNLoader.Cache.DisCache.InternalCacheDiskCacheFactory;
import com.yjy.tnloader.TNLoader.Cache.LruResourceCache;
import com.yjy.tnloader.TNLoader.Cache.MemoryCache;
import com.yjy.tnloader.TNLoader.Cache.MemorySizeCalculator;
import com.yjy.tnloader.TNLoader.Engine.Engine;
import com.yjy.tnloader.TNLoader.Engine.executor.FifoPriorityThreadPoolExecutor;

import java.util.concurrent.ExecutorService;

/**
 * Created by software1 on 2018/1/30.
 */

public class TNBuilder {

    private final Context context;
    private Engine engine;
    private BitmapPool bitmapPool;
    private MemoryCache memoryCache;
    private ExecutorService sourceService;
    private ExecutorService diskCacheService;
    private DiskCache.Factory diskCacheFactory;
    
    public TNBuilder(Context context) {
        this.context = context.getApplicationContext();
    }


    public TNLoader createGlide() {

        if (sourceService == null) {
            final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
            sourceService = new FifoPriorityThreadPoolExecutor(cores);
        }
        if (diskCacheService == null) {
            diskCacheService = new FifoPriorityThreadPoolExecutor(1);
        }

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        if (bitmapPool == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                int size = calculator.getBitmapPoolSize();
                bitmapPool = new LruBitmapPool(size);
            } else {
                bitmapPool = new BitmapPoolAdapter();
            }
        }

        if (memoryCache == null) {
            memoryCache = new LruResourceCache(calculator.getMemoryCacheSize());
        }

        if(diskCacheFactory == null ){
            diskCacheFactory = new InternalCacheDiskCacheFactory(context);
        }

        engine = new Engine();


        return null;
    }
}
