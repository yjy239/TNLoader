package com.yjy.tnloader.TNLoader.Engine.interceptor;

import android.graphics.Bitmap;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;
import com.yjy.tnloader.TNLoader.Cache.ByteArrayPool;
import com.yjy.tnloader.TNLoader.Cache.DisCache.DiskCache;
import com.yjy.tnloader.TNLoader.Engine.Decoder.LowMemoryDecoder;
import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;
import com.yjy.tnloader.TNLoader.Resource.BitmapResource;
import com.yjy.tnloader.TNLoader.Resource.Key;
import com.yjy.tnloader.TNLoader.Resource.RequestKey;
import com.yjy.tnloader.TNLoader.Resource.Resource;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
    private BitmapPool bitmapPool;
    public DiskCacheInterceptor(DiskCache diskCache,BitmapPool bitmapPool){
        this.bitmapPool = bitmapPool;
        this.diskCache = diskCache;
    }

    @Override
    public Response intercept(Chain chain)throws Exception  {
        Request request = chain.request();
        Response response = new Response.Builder().request(request).build();

        Key key = request.key();
        boolean hasDiskCache = hasCacheInDisk(key);
        //存在磁盘缓存则读取磁盘内容，无则直接网络连接
        request.setKeyResource(hasDiskCache);

        //先获取缓存，没有该key的缓存就继续下一个拦截器，有就直接组成response返回
        //先尝试着从结果获取图片，没有再试着从流里面获取图片
        Resource<?> resource = null;
        if(hasDiskCache){
            //先从result读数据
            resource = loadFromCache(key,request);
            //加入没有result的数据，试着从source去读
            if(resource == null){
                if(key instanceof  RequestKey){
                    RequestKey requestKey = (RequestKey)key;
                    resource =  loadFromCache(requestKey.getOriginalKey(),request);
                }
            }
        }


        if(resource == null){
            response = chain.proceed(request);

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
                save2Cache(key,response);
            }

        }else {
            response = new Response.Builder().request(request).result(resource).build();
            Log.e(TAG,"磁盘缓存");
        }


        return response;
    }


    private Resource<?> loadFromCache(Key key,Request request){
        File cacheFile = diskCache.get(key);
        if (cacheFile == null) {
            return null;
        }
        //在此处解析，不好放在一个拦截器，下一个拦截器是对输入的字符串进行抵制查找
        Resource<?> result = null;
        try {
            result = decodeFile2Bitmap(cacheFile,request);
        }catch (Exception e){

        }finally {
            if(result == null){
                diskCache.delete(key);
            }
        }
        return result;
    }

    private void save2Cache(Key key,Response response){
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



    private Resource decodeFile2Bitmap(File file,Request request) throws FileNotFoundException{
        Bitmap bitmap = null;
        LowMemoryDecoder decoder = LowMemoryDecoder.AT_MOST;
        InputStream in =new FileInputStream(file);
        Resource<?> resource = null;
        try {
            bitmap = decoder.decode(in,bitmapPool,request.getWidth(),request.getHeight(), request.getFormat());
        }catch (Exception e){
            e.printStackTrace();
        }
        if(bitmap != null){
            resource = new BitmapResource(bitmap,bitmapPool);
        }
        return resource;
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
