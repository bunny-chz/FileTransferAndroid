package com.bunny.FileTransferLite.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bunny.FileTransferLite.R;

/**
 * Project:  文件快传
 * Comments: SharedPreferences保存键值对工具类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-16
 * Version: 1.0
 */


public class SaveData {
    private final Context context;
    public SaveData(Context context){
        this.context = context;
    }
    /**
     * SharedPreferences保存 String 键值对
     *
     * @param value 保存的值
     * @param key 保存的键
     */
    public void saveString(String value,String key){
        String name = context.getResources().getString(R.string.SaveData);
        SharedPreferences shp = context.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(key,value);
        editor.apply();
    }
    /**
     * SharedPreferences 加载 String 键值对
     *
     * @param key 要加载的键
     */
    public String loadString(String key){
        String name = context.getResources().getString(R.string.SaveData);
        SharedPreferences shp = context.getSharedPreferences(name,Context.MODE_PRIVATE);
        return shp.getString(key,null);//默认返回 null
    }
    /**
     * SharedPreferences保存 Boolean 键值对
     *
     * @param value 保存的值
     * @param key 保存的键
     */
    public void saveSW(Boolean value,String key){
        String name = context.getResources().getString(R.string.SaveData);
        SharedPreferences shp = context.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    /**
     * SharedPreferences保存 String 键值对
     *
     * @param key 要加载的键
     */
    public Boolean loadSW(String key){
        String name = context.getResources().getString(R.string.SaveData);
        SharedPreferences shp = context.getSharedPreferences(name,Context.MODE_PRIVATE);
        return shp.getBoolean(key,false);//默认返回 false
    }
}