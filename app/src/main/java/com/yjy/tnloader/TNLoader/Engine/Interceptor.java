package com.yjy.tnloader.TNLoader.Engine;

import com.yjy.tnloader.TNLoader.Request.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

/**
 * Created by software1 on 2018/1/31.
 */

public interface Interceptor {
    Response intercept(Chain chain) throws Exception;

    interface Chain {

        void interceptor(Chain chain);

        Request request();

        Response proceed(Request request);

        Response getResponse();


    }
}
