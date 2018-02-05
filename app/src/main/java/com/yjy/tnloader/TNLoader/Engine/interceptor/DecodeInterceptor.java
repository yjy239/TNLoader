package com.yjy.tnloader.TNLoader.Engine.interceptor;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;
import com.yjy.tnloader.TNLoader.Engine.Decoder.LowMemoryDecoder;
import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Resource.BitmapDrawableResource;
import com.yjy.tnloader.TNLoader.Resource.BitmapResource;

import java.io.InputStream;

/**
 * Created by yjy on 2018/2/1.
 */

public class DecodeInterceptor implements Interceptor {

    private final String TAG = "Decode";
    private BitmapPool pool;

    public DecodeInterceptor(BitmapPool pool){
        this.pool = pool;
    }

    @Override
    public Response intercept(Chain chain)  {
        Log.e(TAG,"数据流解码");
        Response response = null;
        Request request = chain.request();
        Bitmap bitmap = null;

        if(chain.getResponse()!=null&&pool!=null){
            InputStream in = request.in();
            LowMemoryDecoder decoder = LowMemoryDecoder.AT_MOST;
            try {
                bitmap = decoder.decode(in,pool,request.getWidth(),request.getHeight(), request.getFormat());
            }catch (Exception e){
                e.printStackTrace();
            }
            //拿到bitmap之后转化
            BitmapResource bitmapResource = BitmapResource.obtain(bitmap,pool);
            request.setResource(bitmapResource);
            response = chain.proceed(request);

            response.setType(decoder.imageType);
        }


        return response;
    }
}
