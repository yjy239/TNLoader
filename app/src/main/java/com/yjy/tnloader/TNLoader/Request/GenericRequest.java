package com.yjy.tnloader.TNLoader.Request;

import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Utils.Util;

import java.util.Queue;

/**
 * Created by software1 on 2018/1/31.
 */

public class GenericRequest implements Request {

    private int placeholderResourceId;
    private int errorResourceId;
    private float sizeMultiplier;
    private int overrideWidth;
    private int overrideHeight;
    private static final Queue<GenericRequest> REQUEST_POOL = Util.createQueue(0);
    private Status status;

    public static GenericRequest obtain(int placeholderResourceId,int errorResourceId,float sizeMultiplier,
                                        int overrideWidth,int overrideHeight){
        GenericRequest request =  REQUEST_POOL.poll();
        if(request == null){
            request = new GenericRequest();
        }
        request.init(placeholderResourceId,errorResourceId,sizeMultiplier,
        overrideWidth,overrideHeight);
        return request;

    }

    private enum Status {
        /** Created but not yet running. */
        PENDING,
        /** In the process of fetching media. */
        RUNNING,
        /** Waiting for a callback given to the Target to be called to determine target dimensions. */
        WAITING_FOR_SIZE,
        /** Finished loading media successfully. */
        COMPLETE,
        /** Failed to load media, may be restarted. */
        FAILED,
        /** Cancelled by the user, may not be restarted. */
        CANCELLED,
        /** Cleared by the user with a placeholder set, may not be restarted. */
        CLEARED,
        /** Temporarily paused by the system, may be restarted. */
        PAUSED,
    }

    public void init(int placeholderResourceId,int errorResourceId,float sizeMultiplier,
                     int overrideWidth,int overrideHeight){
        this.placeholderResourceId = placeholderResourceId;
        this.errorResourceId = errorResourceId;
        this.sizeMultiplier = sizeMultiplier;
        this.overrideHeight = overrideHeight;
        this.overrideWidth = overrideWidth;
        status = Status.PENDING;
    }


    @Override
    public void begin() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public boolean isResourceSet() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    @Override
    public void recycle() {
        REQUEST_POOL.offer(this);
    }
}
