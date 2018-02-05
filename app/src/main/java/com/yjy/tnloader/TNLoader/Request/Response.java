package com.yjy.tnloader.TNLoader.Request;

import com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser;
import com.yjy.tnloader.TNLoader.Resource.Resource;

import java.io.InputStream;

/**
 * Created by software1 on 2018/1/31.
 */

public class Response {
    private Request request;
    private int id;

    private InputStream inputStream;
    private Response.Builder builder;
    private Resource<?> result;
    private ImageHeaderParser.ImageType imageType;

    public Response(Response.Builder builder){
        this.builder = builder;
        this.request = builder.request;
        this.inputStream = builder.inputStream;
        this.result = builder.result;
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

    public Resource<?> getResult(){
        return result;
    }

    public ImageHeaderParser.ImageType getImageType(){
        return imageType;
    }

    public void setType(ImageHeaderParser.ImageType type){
        this.imageType = type;
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
        private Resource<?> result;
        private ImageHeaderParser.ImageType imageType;

        public Builder(){

        }

        public Builder(Response response){
            this.request = response.request;
        }

        public Builder request(Request request){
            this.request = request;
            return this;
        }

        public Builder result(Resource<?> result){
            this.result = result;
            return this;
        }

        public Builder setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Builder setType(ImageHeaderParser.ImageType imageType){
            this.imageType = imageType;
            return this;
        }


        public Response build(){
            return new Response(this);
        }

    }


}
