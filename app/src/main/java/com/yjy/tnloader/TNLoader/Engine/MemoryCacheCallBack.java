package com.yjy.tnloader.TNLoader.Engine;

import com.yjy.tnloader.TNLoader.Resource.RequestResource;
import com.yjy.tnloader.TNLoader.Resource.Key;

/**
 * Created by software1 on 2018/2/1.
 */

public interface MemoryCacheCallBack {

    /***存入内存**/
    RequestResource<?> loadFromCache(Key key, boolean isMemoryCacheable);

    /**存入活跃内存*/
    RequestResource<?> loadFromActiveResources(Key key, boolean isMemoryCacheable);

    /***任务完成**/
    void complete(Key key,RequestResource resource);

    /***任务取消**/
    void cancelled(Key key);

}
