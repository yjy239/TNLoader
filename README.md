# TNLoader
一个模仿Glide和Picasso的图片库

该图片库为学习所有。该图片库结合了glide和picasso之间的优点。
目前只支持图片的加载，gif暂时不支持。
支持的加载图片类型有jpeg,webp,png。webp学习picasso可以加速加载出来。当然类型图片其他也可以加载出来，只是上述三种图片能够原来的格式把对应的数据缓存到磁盘文件中（因为android默认的bitmap解析里面包含以上三种，暂时不支持其他）。

