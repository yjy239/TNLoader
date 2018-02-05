package com.yjy.tnloader.TNLoader.Engine.RequestHandler;

import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

import java.io.IOException;

/**
 * Created by software1 on 2018/2/2.
 */

public interface RequestHandler {
    /**
     * Whether or not this {@link RequestHandler} can handle a request with the given {@link Request}.
     */
     boolean canHandleRequest(Request data);

    /**
     * Loads an image for the given {@link Request}.
     *
     * @param request the data from which the image should be resolved.
     *
     */
    Response load(Request request) throws IOException;



}
