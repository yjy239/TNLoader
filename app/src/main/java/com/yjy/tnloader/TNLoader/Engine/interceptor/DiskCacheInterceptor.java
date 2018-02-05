package com.yjy.tnloader.TNLoader.Engine.interceptor;

import android.graphics.Bitmap;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.ByteArrayPool;
import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCache;
import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Resource.Key;
import com.yjy.tnloader.TNLoader.Resource.RequestKey;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by software1 on 2018/1/31.
 */

public class DiskCacheInterceptor implements Interceptor {
    private final String TAG = "DiskCache";


    private DiskCache diskCache;
    public DiskCacheInterceptor(DiskCache diskCache){

        this.diskCache = diskCache;
    }

    @Override
    public Response intercept(Chain chain)  {
        Log.e(TAG,"磁盘缓存");
        Request request = chain.request();

        Key key = request.key();
        boolean hasDiskCache = hasCacheInDisk(key);
        //存在磁盘缓存则读取磁盘内容，无则直接网络连接
        request.setKeyResource(hasDiskCache);


        Response response = chain.proceed(request);

        //获取到流之后把数据存储到disk
        //是否存储流
        if(request.getDiskStrategy().cacheSource()&&response.getInputStream()!= null){
            if(key instanceof RequestKey){
                RequestKey requestKey = (RequestKey)key;
                DiskWriter diskWriter = new DiskWriter(response.getInputStream());
                diskCache.put(requestKey.getOriginalKey(),diskWriter);
                Log.e(TAG,"in disk source");
            }

        }

        //是否存储图片
        if(request.getDiskStrategy().cacheResult()){
            if(response.getResult().get() instanceof Bitmap){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream in = null;
                Bitmap map = (Bitmap) response.getResult().get();
                try {
                    switch (response.getImageType()){
                        case GIF:
                            break;
                        case PNG:
                        case PNG_A:
                            map.compress(Bitmap.CompressFormat.PNG,100,baos);
                            break;
                        case JPEG:
                            map.compress(Bitmap.CompressFormat.JPEG,100,baos);
                            break;
                        case WEBP:
                            map.compress(Bitmap.CompressFormat.WEBP,100,baos);
                            break;
                        case UNKNOWN:
                            map.compress(Bitmap.CompressFormat.JPEG,100,baos);
                            break;
                    }
                    in = new ByteArrayInputStream(baos.toByteArray());
                    DiskWriter diskWriter = new DiskWriter(in);
                    diskCache.put(key,diskWriter);
                    Log.e(TAG,"in disk result");
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    try {
                        in.close();
                        baos.close();
                    }catch (Exception e){

                    }

                }

            }

        }

        return response;
    }


    public boolean hasCacheInDisk(Key key) {
        File resultfile = diskCache.get(key);
        if(resultfile == null){
            return false;
        }
        return true;
    }

    private class DiskWriter implements DiskCache.Writer{
        private InputStream data;
        public DiskWriter(InputStream data){
            this.data = data;
        }

        private boolean canWrite(OutputStream os,InputStream data){
            byte[] buffer = ByteArrayPool.get().getBytes();
            try {
                int read;
                while ((read = data.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                return true;
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Failed to encode data onto the OutputStream", e);
                }
                return false;
            } finally {
                ByteArrayPool.get().releaseBytes(buffer);
            }
        }

        @Override
        public boolean write(File file) {
            boolean success = false;
            BufferedOutputStream opener = null;
            try {
                opener = new BufferedOutputStream(new FileOutputStream(file));
                success = canWrite(opener,data);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }finally {
                if (opener != null) {
                    try {
                        opener.close();
                    } catch (IOException e) {
                        // Do nothing.
                    }
                }
            }


            return success;
        }
    }
}
