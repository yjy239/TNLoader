# TNLoader
一个模仿Glide和Picasso的图片库

这个是我写了下面这个博客后，分析了两者的不同，结合两者优点做出来的一个图片加载库：
https://www.jianshu.com/p/4de87ebf5104


该图片库为学习所有。该图片库结合了glide和picasso之间的优点。
目前只支持图片的加载，gif暂时不支持。
支持的加载图片类型有jpeg,webp,png。webp学习picasso可以加速加载出来。当然类型图片其他也可以加载出来，只是上述三种图片能够原来的格式把对应的数据缓存到磁盘文件中（因为android默认的bitmap解析里面包含以上三种，暂时不支持其他）。

用法
```
TNLoader.with(mContext)
                    .load(list.get(position))
                    //开启内存缓存，默认是不开启内存缓存，开启磁盘缓存
                    .memoryCache(true)
                    .into(((ListViewHolder)holder).iv);
```

该图片库参考了gilde的声明周期管理和缓存管理以及资源回收,参考了picasso的requestHandler处理资源地址思路，以及okhttp的拦截器思路.

#TNLoader图片加载库模型
![image](https://github.com/yjy239/TNLoader/blob/master/img/TNLoader.png)

上面列出几个拦截器，为这个库的核心：
1.memoryCahcheInterceptor
这个是内存缓存拦截器，把请求拦截下来，查看是否挂载在内存上有没有对应的资源，有则取出，拦截器拦截行为返回数据。

2.DiskCacheInterceptor
磁盘缓存拦截器，假如内存不存在资源，则从磁盘查找有没有对应的资源，有则拦截从本地获取，没有则到下一个拦截器。

3.StreamInterceptor
流获取拦截器，这个拦截器是用来处理传下来的uri，去寻找资源地址的处理器(RequestHandler)。这个requestHandler是可以自己定义的。是用来识别uri中使用的处理方式。比如说load的方法中是load(url),通过判断是一个http的url超链接则会调用networkRequestHandler来联网处理，获取到数据流。

4.DecodeInterceptor
解析从上层拦截器传下来的数据流转化为图片

5.BitmapTranformInterceptor
图片转化拦截器，把生成的图片转化一次，比如说把图片转化为圆角等，在工程中暂时不做处理

以上这些拦截器均可进行替换，课添加新的拦截器，而且运行用户在拦截器中拦截整个行为。

该工程TNLoader全称为terrible network image loader
为了处理糟糕的网络环境，这里着重模仿了glide的缓存处理机制，以及引入了glide不支持的webp的解析，加快解析的速度，适应糟糕的网络情况。而且这里我提供了RequestHandler允许其他人添加新的联网机制，优先处理自定义的RequestHandler。

为了不依赖任何的第三方库，我这边做了一个默认的networkReqeustHandler，实际上想要得到更好的糟糕的网络体验，最好使用okhttp添加新的联网requestHandler
```
public RequestBuilder addRequestHandler(List<RequestHandler> handlers){
        this.customHandler = handlers;
        return this;
    }

```




