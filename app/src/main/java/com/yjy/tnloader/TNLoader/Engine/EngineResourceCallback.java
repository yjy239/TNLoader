package com.yjy.tnloader.TNLoader.Engine;

import com.yjy.tnloader.TNLoader.Resource.Resource;

/**
 * Created by software1 on 2018/2/5.
 */

public interface EngineResourceCallback {
    void onResourceReady(Resource<?> resource);
}
