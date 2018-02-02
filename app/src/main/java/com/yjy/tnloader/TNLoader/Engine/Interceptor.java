package com.yjy.tnloader.TNLoader.Engine;

import com.yjy.tnloader.TNLoader.Engine.Response;
import com.yjy.tnloader.TNLoader.Request.Request;

import java.io.IOException;

/**
 * Created by software1 on 2018/1/31.
 */

public interface Interceptor {
    Response intercept(Chain chain);

    interface Chain {

        void interceptor(Chain chain);

        Request request();

        Response proceed(Request request);

        Response getResponse();


    }
}
