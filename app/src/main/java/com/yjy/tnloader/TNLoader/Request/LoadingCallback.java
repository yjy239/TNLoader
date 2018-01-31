package com.yjy.tnloader.TNLoader.Request;

/**
 * Created by software1 on 2018/1/31.
 */

public interface LoadingCallback {
    void onSuccess();

    void onError();

    public static class EmptyCallback implements LoadingCallback {

        @Override public void onSuccess() {
        }

        @Override public void onError() {
        }
    }
}
