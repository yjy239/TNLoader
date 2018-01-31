package com.yjy.tnloader.TNLoader.manager;

/**
 * Created by software1 on 2018/1/30.
 */

/**
 * An interface for listening to Activity/Fragment lifecycle events.
 */
public interface Lifecycle {
    /**
     * Adds the given listener to the set of listeners managed by this Lifecycle implementation.
     */
    void addListener(LifecycleListener listener);
}
