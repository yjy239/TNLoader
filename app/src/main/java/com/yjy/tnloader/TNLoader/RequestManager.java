package com.yjy.tnloader.TNLoader;

/**
 * Created by yjy on 2018/1/30.
 */

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.yjy.tnloader.TNLoader.Utils.Util;
import com.yjy.tnloader.TNLoader.manager.ConnectivityMonitor;
import com.yjy.tnloader.TNLoader.manager.ConnectivityMonitorFactory;
import com.yjy.tnloader.TNLoader.manager.Lifecycle;
import com.yjy.tnloader.TNLoader.manager.LifecycleListener;
import com.yjy.tnloader.TNLoader.manager.RequestManagerTreeNode;
import com.yjy.tnloader.TNLoader.manager.RequestTracker;

/**
 * A class for managing and starting requests for Glide. Can use activity, fragment and connectivity lifecycle events to
 * intelligently stop, start, and restart requests. Retrieve either by instantiating a new object, or to take advantage
 * built in Activity and Fragment lifecycle handling, use the static Glide.load methods with your Fragment or Activity.

 */
public class RequestManager implements LifecycleListener {
    private final Context context;
    private final Lifecycle lifecycle;
    private final RequestManagerTreeNode treeNode;
    private final RequestTracker requestTracker;
    private final TNLoader loader;

    public RequestManager(Context context, Lifecycle lifecycle, RequestManagerTreeNode treeNode) {
        this(context, lifecycle, treeNode, new RequestTracker(), new ConnectivityMonitorFactory());
    }

    RequestManager(Context context, final Lifecycle lifecycle, RequestManagerTreeNode treeNode,
                   RequestTracker requestTracker, ConnectivityMonitorFactory factory) {
        this.context = context.getApplicationContext();
        this.lifecycle = lifecycle;
        this.treeNode = treeNode;
        this.requestTracker = requestTracker;
        this.loader = TNLoader.get(context);

        ConnectivityMonitor connectivityMonitor = factory.build(context,
                new RequestManagerConnectivityListener(requestTracker));

        // If we're the application level request manager, we may be created on a background thread. In that case we
        // cannot risk synchronously pausing or resuming requests, so we hack around the issue by delaying adding
        // ourselves as a lifecycle listener by posting to the main thread. This should be entirely safe.
        if (Util.isOnBackgroundThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    lifecycle.addListener(RequestManager.this);
                }
            });
        } else {
            lifecycle.addListener(this);
        }
        lifecycle.addListener(connectivityMonitor);
    }


    /**
     * @see android.content.ComponentCallbacks2#onTrimMemory(int)
     */
    public void onTrimMemory(int level) {
        loader.trimMemory(level);
    }

    /**
     * @see android.content.ComponentCallbacks2#onLowMemory()
     */
    public void onLowMemory() {
        loader.clearMemory();
    }



    /***
     * load from url**/
    public RequestBuilder load(String url){
        return new RequestBuilder(context,loader,url,lifecycle,requestTracker);
    }


    /**
     * Returns true if loads for this {@link RequestManager} are currently paused.
     *
     * @see #pauseRequests()
     * @see #resumeRequests()
     */
    public boolean isPaused() {
        Util.assertMainThread();
        return requestTracker.isPaused();
    }

    /**
     * Cancels any in progress loads, but does not clear resources of completed loads.
     *
     * @see #isPaused()
     * @see #resumeRequests()
     */
    public void pauseRequests() {
        Util.assertMainThread();
        requestTracker.pauseRequests();
    }

    /**
     * Performs {@link #pauseRequests()} recursively for all managers that are contextually descendant
     * to this manager based on the Activity/Fragment hierarchy:
     *
     * <ul>
     * <li>When pausing on an Activity all attached fragments will also get paused.
     * <li>When pausing on an attached Fragment all descendant fragments will also get paused.
     * <li>When pausing on a detached Fragment or the application context only the current RequestManager is paused.
     * </ul>
     *
     * <p>Note, on pre-Jelly Bean MR1 calling pause on a Fragment will not cause child fragments to pause, in this
     * case either call pause on the Activity or use a support Fragment.
     */
    public void pauseRequestsRecursive() {
        Util.assertMainThread();
        pauseRequests();
        for (RequestManager requestManager : treeNode.getDescendants()) {
            requestManager.pauseRequests();
        }
    }

    /**
     * Restarts any loads that have not yet completed.
     *
     * @see #isPaused()
     * @see #pauseRequests()
     */
    public void resumeRequests() {
        Util.assertMainThread();
        requestTracker.resumeRequests();
    }

    /**
     * Performs {@link #resumeRequests()} recursively for all managers that are contextually descendant
     * to this manager based on the Activity/Fragment hierarchy. The hierarchical semantics are identical as for
     * {@link #pauseRequestsRecursive()}.
     */
    public void resumeRequestsRecursive() {
        Util.assertMainThread();
        resumeRequests();
        for (RequestManager requestManager : treeNode.getDescendants()) {
            requestManager.resumeRequests();
        }
    }

    /**
     * Lifecycle callback that registers for connectivity events (if the android.permission.ACCESS_NETWORK_STATE
     * permission is present) and restarts failed or paused requests.
     */
    @Override
    public void onStart() {
        // onStart might not be called because this object may be created after the fragment/activity's onStart method.
        resumeRequests();
    }

    /**
     * Lifecycle callback that unregisters for connectivity events (if the android.permission.ACCESS_NETWORK_STATE
     * permission is present) and pauses in progress loads.
     */
    @Override
    public void onStop() {
        pauseRequests();
    }

    /**
     * Lifecycle callback that cancels all in progress requests and clears and recycles resources for all completed
     * requests.
     */
    @Override
    public void onDestroy() {
        requestTracker.clearRequests();
    }



    private static class RequestManagerConnectivityListener implements ConnectivityMonitor.ConnectivityListener {
        private final RequestTracker requestTracker;

        public RequestManagerConnectivityListener(RequestTracker requestTracker) {
            this.requestTracker = requestTracker;
        }

        @Override
        public void onConnectivityChanged(boolean isConnected) {
            if (isConnected) {
                requestTracker.restartRequests();
            }

        }
    }
}
