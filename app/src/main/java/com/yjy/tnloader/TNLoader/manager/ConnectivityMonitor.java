package com.yjy.tnloader.TNLoader.manager;

/**
 * Created by yjy on 2018/1/30.
 */

public interface ConnectivityMonitor extends LifecycleListener {
    /**
     * An interface for listening to network connectivity events picked up by the monitor.
     */
    interface ConnectivityListener {
        /**
         * Called when the connectivity state changes.
         *
         * @param isConnected True if we're currently connected to a network, false otherwise.
         */
        void onConnectivityChanged(boolean isConnected);
    }
}
