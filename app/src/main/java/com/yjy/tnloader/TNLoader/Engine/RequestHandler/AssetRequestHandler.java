package com.yjy.tnloader.TNLoader.Engine.RequestHandler;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentResolver.SCHEME_FILE;

/**
 * Created by software1 on 2018/2/2.
 */

public class AssetRequestHandler implements RequestHandler {
    protected static final String ANDROID_ASSET = "android_asset";
    private static final int ASSET_PREFIX_LENGTH =
            (SCHEME_FILE + ":///" + ANDROID_ASSET + "/").length();

    private final AssetManager assetManager;

    public AssetRequestHandler(Context context) {
        assetManager = context.getAssets();
    }

    @Override public boolean canHandleRequest(Request data) {
        Uri uri = Uri.parse(data.url());
        return (SCHEME_FILE.equals(uri.getScheme())
                && !uri.getPathSegments().isEmpty() && ANDROID_ASSET.equals(uri.getPathSegments().get(0)));
    }

    @Override public Response load(Request request) throws IOException {
        InputStream is = assetManager.open(getFilePath(request));
        //处理
        return new Response.Builder().request(request).build();
    }

    static String getFilePath(Request request) {
        return request.url().toString().substring(ASSET_PREFIX_LENGTH);
    }
}
