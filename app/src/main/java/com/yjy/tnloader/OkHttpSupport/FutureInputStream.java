package com.yjy.tnloader.OkHttpSupport;

import java.io.InputStream;

/**
 * Created by software1 on 2018/2/7.
 */

public class FutureInputStream implements Data {

    private RealInputStream real = null;
    private boolean ready = false;

    public synchronized void setRealData(RealInputStream real){
        if(ready||real == null){
            return;
        }
        this.real = real;
        real.excute();
        this.ready = true;
        notifyAll();
    }



    @Override
    public synchronized InputStream getInputStream() {
        while(!ready){
            try {
                wait();
            }catch (InterruptedException e){

            }
        }
        ready = false;
        if(real == null){
            return null;
        }
        return real.getInputStream();
    }
}
