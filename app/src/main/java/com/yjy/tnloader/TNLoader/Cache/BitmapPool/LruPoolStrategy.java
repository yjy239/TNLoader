package com.yjy.tnloader.TNLoader.Cache.BitmapPool;

import android.graphics.Bitmap;

/**
 * Created by software1 on 2018/1/31.
 */

public interface LruPoolStrategy {
    void put(Bitmap bitmap);
    Bitmap get(int width, int height, Bitmap.Config config);
    Bitmap removeLast();
    String logBitmap(Bitmap bitmap);
    String logBitmap(int width, int height, Bitmap.Config config);
    int getSize(Bitmap bitmap);
}
