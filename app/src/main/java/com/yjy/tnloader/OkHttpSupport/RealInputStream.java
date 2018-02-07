package com.yjy.tnloader.OkHttpSupport;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by software1 on 2018/2/7.
 */

public class RealInputStream implements Data {

    private final String mUrl;
    private InputStream in;


    public RealInputStream(String url){
        this.mUrl = url;
    }


    public void excute(){
        OkHttpClient client = new OkHttpClient();
        okhttp3.Request okrequest = new okhttp3.Request.Builder()
                .url(mUrl)
                .build();

        Call call = client.newCall(okrequest);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                in = response.body().byteStream();
            }
        });

    }

    @Override
    public synchronized InputStream getInputStream() {
        //超时时间为5秒
        while(in == null){
            try {
                wait(5*1000);
            }catch (InterruptedException e){

            }
        }
        return in;
    }
}
