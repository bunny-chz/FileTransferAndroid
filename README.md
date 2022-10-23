# FileTransferAndroid
文件快传是基于NanoHTTPD搭建的安卓服务器，基于Retrofit和okHttp处理HTTP请求响应的安卓客户端。


--------------------------------------

这个是自定义文件传输软件系统的一部分---安卓端服务器和客户端，

可以在局域网内基于HTTP协议，实现电脑，安卓，网页浏览器三端数据互通

电脑客户端请看这里（用到了QNetworkAccessManager类进行GET,POST请求）

https://github.com/bunny-chz/FileTransferWindowsClient

电脑服务器端请看这里（用到了QtWebApp）

https://github.com/bunny-chz/FileTransferWindowsServer

-----------------------------------------

**开发环境**

Android Studio    API > 21   安卓5.0以上


**用到的依赖**

本软件用到Retrofit，处理HTTP的请求和响应

https://github.com/square/retrofit

用到了NanoHTTPD，搭建轻量级安卓服务器

https://github.com/NanoHttpd/nanohttpd

本人感觉很好的安卓HTTP网络编程教程，希望能帮到你学习安卓，宝藏博主的B站--->

https://www.bilibili.com/video/BV1ua4y1J7VV

关于界面网址

https://github.com/medyo/android-about-page

扫码解码

https://github.com/jenly1314/ZXingLite


新拟物化设计界面风格网址

https://github.com/sshadkany/Android_neumorphic


本项目中NanoHTTPD搭建的服务器，客户端上传的文件，缓存在缓存目中，需要声明读写权限，否则客户端上传文件时会出错

其中安卓10，需要在AndroidManifest.xml的application中声明

```
android:requestLegacyExternalStorage="true"
```

安卓11，需要在AndroidManifest.xml中声明 

```
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
```

且需要进行页面跳转给予管理全部文件的权限，

```
boolean highPermission = Environment.isExternalStorageManager();
if (!highPermission) {
    @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
    intent.setData(Uri.fromParts("package", this.getPackageName(), null));
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    this.startActivity(intent);
}
```


本项目用了大量文件读写方法，来实现功能。具体实现请看代码。



**自定义的服务器接口**

说明：下面的IP和Port为设备所属网络环境的IP和端口，只支持HTTP

textCtoS 即是 text from Client to Server的缩写，其他以此类推

http://IP:Port/ 网页端主页，客户端GET获取到一个网页

http://IP:Port/testLink 客户端GET请求，连接测试

http://IP:Port/textCtoSPage 客户端GET，获取到一个发送文本给服务器的操作网页

http://IP:Port/textCtoS 链接中带参数，客户端POST请求后，会给服务器发送文本信息，键值为"text=???"

http://IP:Port/textStoC 客户端GET获取到服务器发送的信息

http://IP:Port/textStoCWeb 网页端GET获取到服务器发送的信息

http://IP:Port/fileCtoS 客户端POST请求后，会给服务器发送文件，POST请求内容标记为"file"

http://IP:Port/fileCtoSPage 网页端GET获取到一个发送文件给服务器的操作网页

http://IP:Port/fileStoCWeb 网页端GET服务器发送的文件

http://IP:Port/fileStoC 客户端GET服务器发送的文件


**界面预览**

------------------------------

**主界面**

![Screenshot_2022-10-23-15-35-17-44_45da8379b8d5050e61946b694b18ea47](https://user-images.githubusercontent.com/57706599/197380097-279aa376-0382-4999-8f04-827f0b61efd9.jpg)

-------------------------------------------

**服务器界面**

![Screenshot_2022-10-23-15-35-20-42_45da8379b8d5050e61946b694b18ea47](https://user-images.githubusercontent.com/57706599/197380112-a92ca525-8d24-4646-9d13-6de25f72e51b.jpg)

---------------------------------------

**客户端界面**

![Screenshot_2022-10-23-15-35-27-46_45da8379b8d5050e61946b694b18ea47](https://user-images.githubusercontent.com/57706599/197380124-7e43ab79-88b3-49af-b7c3-b5c68f700722.jpg)

