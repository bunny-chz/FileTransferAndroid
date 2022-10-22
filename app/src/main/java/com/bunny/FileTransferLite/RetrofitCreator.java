package com.bunny.FileTransferLite;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Project:  文件快传
 * Comments: Retrofit初始化类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-02
 * Version: 1.0
 */

public class RetrofitCreator {
    public static final int CONNECT_TIME_OUT = 10000;//毫秒
    private Retrofit mRetrofit;

    private RetrofitCreator(String baseUrl) {
        createRetrofit(baseUrl);
    }

    private void createRetrofit(String baseUrl) {
        //设置一下okHttp的参数
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIME_OUT,TimeUnit.MILLISECONDS)
                .build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)//设置BaseUrl
                .client(okHttpClient)//设置请求的client
                .addConverterFactory(GsonConverterFactory.create())//设置转换器
                .build();
    }

    private static RetrofitCreator sRetrofitCreator = null;

    public static RetrofitCreator getInstance(String baseUrl) {
        if(sRetrofitCreator == null) {
            synchronized(RetrofitCreator.class) {
                if(sRetrofitCreator == null) {
                    sRetrofitCreator = new RetrofitCreator(baseUrl);
                }
            }
        }
        return sRetrofitCreator;
    }


    public Retrofit getRetrofit() {
        return mRetrofit;
    }


}



