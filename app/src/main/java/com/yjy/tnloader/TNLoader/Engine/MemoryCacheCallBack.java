package com.yjy.tnloader.TNLoader.Engine;

import com.yjy.tnloader.TNLoader.Resource.RequestResource;
import com.yjy.tnloader.TNLoader.Resource.Key;

/**
 * Created by software1 on 2018/2/1.
 */

public interface MemoryCacheCallBack {

    RequestResource<?> loadFromCache(Key key, boolean isMemoryCacheable);

    RequestResource<?> loadFromActiveResources(Key key, boolean isMemoryCacheable);
}
