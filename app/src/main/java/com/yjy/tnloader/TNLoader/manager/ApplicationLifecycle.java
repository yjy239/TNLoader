package com.yjy.tnloader.TNLoader.manager;

/**
 * Created by yjy on 2018/1/30.
 */

class ApplicationLifecycle implements Lifecycle {
    @Override
    public void addListener(LifecycleListener listener) {
        listener.onStart();
    }
}
