package com.yjy.tnloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.yjy.tnloader.TNLoader.Request.ImageViewTarget;
import com.yjy.tnloader.TNLoader.Request.Target;
import com.yjy.tnloader.TNLoader.TNLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView view;
    private RecyclerView list;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (RecyclerView)findViewById(R.id.list);

        List<String> test = new ArrayList<>();
        test.add("https://p.upyun.com/demo/webp/webp/gif-0.webp");
        test.add("http://img.taopic.com/uploads/allimg/120727/201995-120HG1030762.jpg");
        test.add("https://p.upyun.com/demo/webp/webp/gif-0.webp");
        test.add("http://img.taopic.com/uploads/allimg/120727/201995-120HG1030762.jpg");
        test.add("http://res1.age06.com/FileStore/PortalIPSForQX/V3/d6cedf78-a699-4322-8d8b-af976fc94bc1/c3d432b8-290d-4a4c-9598-187342ae4d9d/c77ab157-e2d3-4c79-b92b-167e30b76d0c.jpg");
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(manager);
        adapter = new ListAdapter(this,test);
        list.setAdapter(adapter);

//        TNLoader.with(this)
//                .load("https://p.upyun.com/demo/webp/webp/gif-0.webp")
//                .into(view);

    }
}
