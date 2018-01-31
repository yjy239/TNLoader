package com.yjy.tnloader.TNLoader.Cache.BitmapPool;

/**
 * Created by yjy on 2018/1/31.
 */

import android.graphics.Bitmap;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;

/**
 * An {@link BitmapPool BitmapPool} implementation that rejects all
 * {@link android.graphics.Bitmap Bitmap}s added to it and always returns {@code null} from get.
 */
public class BitmapPoolAdapter implements BitmapPool {
    @Override
    public int getMaxSize() {
        return 0;
    }

    @Override
    public void setSizeMultiplier(float sizeMultiplier) {
        // Do nothing.
    }

    @Override
    public boolean put(Bitmap bitmap) {
        return false;
    }

    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        return null;
    }

    @Override
    public Bitmap getDirty(int width, int height, Bitmap.Config config) {
        return null;
    }

    @Override
    public void clearMemory() {
        // Do nothing.
    }

    @Override
    public void trimMemory(int level) {
        // Do nothing.
    }
}

