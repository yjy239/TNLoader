package com.yjy.tnloader.TNLoader.Cache.BitmapPool;

import com.yjy.tnloader.TNLoader.Cache.Poolable;
import com.yjy.tnloader.TNLoader.Utils.Util;

import java.util.Queue;

/**
 * Created by yjy on 2018/1/31.
 */

abstract class BaseKeyPool<T extends Poolable> {

    private static final int MAX_SIZE = 20;
    private final Queue<T> keyPool = Util.createQueue(MAX_SIZE);

    protected T get() {
        T result = keyPool.poll();
        if (result == null) {
            result = create();
        }
        return result;
    }

    public void offer(T key) {
        if (keyPool.size() < MAX_SIZE) {
            keyPool.offer(key);
        }
    }

    protected abstract T create();
}
