package com.yjy.tnloader.TNLoader.Request;

/**
 * Created by software1 on 2018/1/31.
 */

public interface SizeReadyCallback {
    /**
     * A callback called on the main thread.
     *
     * @param width The width in pixels of the target, or {@link Target#SIZE_ORIGINAL} to indicate that we want the
     *              resource at its original width.
     * @param height The height in pixels of the target, or {@link Target#SIZE_ORIGINAL} to indicate that we want the
     *               resource at its original height.
     */
    void onSizeReady(int width, int height);
}
