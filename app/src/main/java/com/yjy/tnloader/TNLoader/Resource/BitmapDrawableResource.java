package com.yjy.tnloader.TNLoader.Resource;

/**
 * Created by software1 on 2018/2/5.
 */

import android.graphics.drawable.BitmapDrawable;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;
import com.yjy.tnloader.TNLoader.Utils.Util;

/**
 * A {@link Resource} that wraps an {@link android.graphics.drawable.BitmapDrawable}
 * <p>
 *     This class ensures that every call to {@link #get()}} always returns a new
 *     {@link android.graphics.drawable.BitmapDrawable} to avoid rendering issues if used in multiple views and
 *     is also responsible for returning the underlying {@link android.graphics.Bitmap} to the given
 *     {@link BitmapPool} when the resource is recycled.
 * </p>
 */
public class BitmapDrawableResource implements Resource<BitmapDrawable> {
    private final BitmapPool bitmapPool;
    protected final BitmapDrawable drawable;

    public BitmapDrawableResource(BitmapDrawable drawable, BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
        this.drawable = drawable;
    }

    @Override
    public BitmapDrawable get() {
        return drawable;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(drawable.getBitmap());
    }

    @Override
    public void recycle() {
        bitmapPool.put(drawable.getBitmap());
    }
}
