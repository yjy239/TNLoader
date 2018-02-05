package com.yjy.tnloader.TNLoader.Engine.interceptor;

import android.util.Log;

import com.yjy.tnloader.TNLoader.Engine.Interceptor;
import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yjy on 2018/2/1.
 */

public class RealInterceptorChain implements Interceptor.Chain {
    private List<Interceptor> interceptors = new ArrayList<>();
    private int index = 0;
    private Request request;
    private boolean interrupt = false;
    private Response response;



    public RealInterceptorChain(List<Interceptor> interceptors, int index,Response response) {
        this.interceptors = interceptors;
        this.index = index;
        this.response = response;
        this.request = response.getRequest();
    }

    @Override
    public void interceptor(Interceptor.Chain chain) {
        interrupt = true;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) {
        if(interrupt||interceptors.size()<=index){
            return response;
        }
            RealInterceptorChain next = new RealInterceptorChain(interceptors,index+1,response);
            Interceptor interceptor = interceptors.get(index);
            Response response = interceptor.intercept(next);
            Log.e("response",interceptor.getClass()+" index:"+index);

        return response;
    }

    public Response getResponse(){
        return response;
    }


}
