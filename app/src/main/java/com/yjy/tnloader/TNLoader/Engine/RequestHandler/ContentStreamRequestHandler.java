package com.yjy.tnloader.TNLoader.Engine.RequestHandler;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentResolver.SCHEME_CONTENT;

/**
 * Created by software1 on 2018/2/2.
 */

public class ContentStreamRequestHandler implements RequestHandler {
    final Context context;

    public ContentStreamRequestHandler(Context context) {
        this.context = context;
    }

    @Override public boolean canHandleRequest(Request data) {
        return SCHEME_CONTENT.equals(Uri.parse(data.url()));
    }

    @Override public Response load(Request request) throws IOException {
        return new Response.Builder().request(request).build();
    }

    InputStream getInputStream(Request request) throws FileNotFoundException {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.openInputStream(Uri.parse(request.url()));
    }
}
