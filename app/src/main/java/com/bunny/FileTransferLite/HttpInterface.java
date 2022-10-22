package com.bunny.FileTransferLite;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Project:  文件快传
 * Comments: 客户端Retrofit接口类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-14
 * Version: 1.0
 */

public interface HttpInterface {
    //连接测试注解
    @GET("/testLink")
    Call<ResponseBody> testLink();
    //发送文本到服务器
    @POST("/textCtoS")
    Call<ResponseBody> postString(@Query("text")String string);
    //从服务器接收文本
    @GET("/textStoC")
    Call<ResponseBody> getText();
    //从服务器下载文件
    @Streaming
    @GET("/fileStoC")
    Call<ResponseBody> downFile();
    //上传文件到服务器
    @Multipart
    @POST("/fileCtoS")
    Call<ResponseBody> postFile(@PartMap Map<String,Object> params, @Part MultipartBody.Part file);
}
