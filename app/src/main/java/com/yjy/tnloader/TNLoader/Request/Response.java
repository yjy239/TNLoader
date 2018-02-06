package com.yjy.tnloader.TNLoader.Request;

import com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser;
import com.yjy.tnloader.TNLoader.Resource.Resource;

import java.io.InputStream;

/**
 * Created by software1 on 2018/1/31.
 */

public class Response {
    private Request request;

    private InputStream inputStream;
    private Response.Builder builder;
    private Resource<?> result;
    private ImageHeaderParser.ImageType imageType;
    private Exception e;


    public Response(Response.Builder builder){
        this.builder = builder;
        this.request = builder.request;
        this.inputStream = builder.inputStream;
        this.result = builder.result;
        this.e = builder.e;
    }

    public void setRequest(Request request){
        this.request = request;
    }

    public Request getRequest(){
        return request;
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

    public Exception getException(){
        return e;
    }

    public void clear(){
        builder = null;
        request = null;
        try {
            inputStream.close();
            inputStream = null;
        }catch (Exception e){

        }
        imageType = ImageHeaderParser.ImageType.UNKNOWN;
    }



    public Builder newBuilder(){
        return new Builder(this);
    }

    public static class Builder{
        private Request request;
        private InputStream inputStream;
        private Resource<?> result;
        private ImageHeaderParser.ImageType imageType;
        private Exception e;

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

        public Builder Exception(Exception e){
            this.e = e;
            return this;
        }


        public Response build(){
            return new Response(this);
        }

    }


}
