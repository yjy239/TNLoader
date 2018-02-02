package com.yjy.tnloader.TNLoader.Engine;

import com.yjy.tnloader.TNLoader.Request.Request;

import java.io.InputStream;

/**
 * Created by software1 on 2018/1/31.
 */

public class Response {
    private Request request;
    private int id;



    private InputStream inputStream;
    private Response.Builder builder;

    public Response(Response.Builder builder){
        this.builder = builder;
        this.request = builder.request;
        this.inputStream = builder.inputStream;
    }

    public void setRequest(Request request){
        this.request = request;
    }

    public Request getRequest(){
        return request;
    }
    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void clear(){
        builder = null;
        request = null;
        inputStream = null;
    }



    public Builder newBuilder(){
        return new Builder(this);
    }

    public static class Builder{
        private Request request;



        private InputStream inputStream;

        public Builder(){

        }

        public Builder(Response response){
            this.request = response.request;
        }

        public Builder request(Request request){
            this.request = request;
            return this;
        }


        public Builder setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Response build(){
            return new Response(this);
        }

    }


}
