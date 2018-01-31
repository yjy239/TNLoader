package com.yjy.tnloader.TNLoader.manager;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by software1 on 2018/1/30.
 */

public class ConnectivityMonitorFactory {
    public ConnectivityMonitor build(Context context, ConnectivityMonitor.ConnectivityListener listener) {
        final int res = context.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE");
        final boolean hasPermission = res == PackageManager.PERMISSION_GRANTED;
        if (hasPermission) {
            return new DefaultConnectivityMonitor(context, listener);
        } else {
            return new NullConnectivityMonitor();
        }
    }
}
