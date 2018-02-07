package com.yjy.tnloader;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yjy.tnloader.TNLoader.Engine.RequestHandler.RequestHandler;
import com.yjy.tnloader.TNLoader.TNLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by software1 on 2018/2/6.
 */

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> list = new ArrayList<>();
    private Context mContext;
    private Handler handler = new Handler();
    private OkhttpRequestHandler okhandler;
    private List<RequestHandler> handlers = new ArrayList<>();

    public ListAdapter(Context mContext,List<String> list) {
        this.list = list;
        this.mContext = mContext;
        OkhttpRequestHandler okhandler = new OkhttpRequestHandler();
        handlers.add(okhandler);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ListViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_layout,null,false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        if(position < list.size()-1){
            TNLoader.with(mContext)
                    .load(list.get(position))
                    .memoryCache(true)
                    .addRequestHandler(handlers)
                    .into(((ListViewHolder)holder).iv);

        }else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TNLoader.with(mContext)
                            .load(list.get(position))
                            .memoryCache(true)
                            .addRequestHandler(handlers)
                            .into(((ListViewHolder)holder).iv);
                }
            },6000);
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder{

        public ImageView iv;

        public ListViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView)itemView.findViewById(R.id.img);
        }
    }
}
