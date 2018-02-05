package com.yjy.tnloader.TNLoader.Engine.RequestHandler;

import android.net.Uri;

import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

import java.io.IOException;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;

/**
 * Created by software1 on 2018/2/2.
 */

public class ResourceRequestHandler implements RequestHandler {


    @Override
    public boolean canHandleRequest(Request data) {


        return SCHEME_ANDROID_RESOURCE.equals(Uri.parse(data.url()).getScheme());
    }

    @Override
    public Response load(Request request) throws IOException {
        return new Response.Builder().request(request).build();
    }
}
