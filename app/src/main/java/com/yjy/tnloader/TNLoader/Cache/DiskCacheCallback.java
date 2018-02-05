package com.yjy.tnloader.TNLoader.Cache;

import com.yjy.tnloader.TNLoader.Resource.Key;

import java.io.File;

/**
 * Created by software1 on 2018/2/2.
 */

public interface DiskCacheCallback {

    boolean hasCacheInDisk(Key key);

}
