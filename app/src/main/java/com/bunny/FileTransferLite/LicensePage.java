package com.bunny.FileTransferLite;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Project:  文件快传
 * Comments: 开源信息许可类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-16
 * Version: 1.0
 */

public class LicensePage extends AppCompatActivity {
    final String[] about_license_ZXingLite = new String[]{"ZXingLite","Apache v2.0"};
    final String[] about_license_nanohttpd = new String[]{"nanohttpd","BSD-3-Clause license"};
    final String[] about_license_AndroidAboutPage = new String[]{"Android About Page","MIT License"};
    final String[] about_license_Retrofit = new String[]{"retrofit","Apache-2.0 license"};
    final String[] about_license_Android_neumorphic = new String[]{"Android_neumorphic","Public archive"};
    private final String[] license_name = new String[]{"nanohttpd","Android About Page","retrofit","ZXingLite","Android_neumorphic"};
    private final String[] license = new String[]{"BSD-3-Clause license","MIT License","Apache-2.0 license","Apache v2.0","Public archive"};
    private final String[] license_modify = new String[]{"否","否","否","否","是"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_page);
        initView();
    }

    private void initView() {
        Button licensePageBack = findViewById(R.id.licensePageBack);
        licensePageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        List<HashMap<String, Object>> list = new ArrayList<>();
        for (int i = 0;i< license_name.length;i++) {
            HashMap<String,Object> item= new HashMap<>();
            item.put("license_name",license_name[i]);
            item.put("license",license[i]);
            item.put("license_modify",license_modify[i]);
            list.add(item);
        }
        SimpleAdapter adapter=new SimpleAdapter(this,list,R.layout.license_listview,new String[]{"license_name","license","license_modify"},new int[]{R.id.license_name,R.id.license,R.id.license_modify});
        ListView license_list = findViewById(R.id.license_list);
        license_list.setAdapter(adapter);
        license_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        LicenseNanoHttpd();
                        break;
                    case 1:
                        LicenseAndroidAboutPage();
                        break;
                    case 2:
                        LicenseRetrofit();
                        break;
                    case 3:
                        LicenseZXingLite();
                        break;
                    case 4:
                        LicenseAndroid_neumorphic();
                        break;
                }
            }
        });
    }

    public void LicenseNanoHttpd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LicensePage.this);
        builder.setTitle("请选择并查看相关开源信息");
        builder.setItems(about_license_nanohttpd,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Uri uri1 = Uri.parse("https://github.com/NanoHttpd/nanohttpd");
                        Intent intent1 = new Intent();
                        intent1.setAction("android.intent.action.VIEW");
                        intent1.setData(uri1);
                        startActivity(intent1);
                        break;
                    case 1:
                        Uri uri2 = Uri.parse("https://github.com/NanoHttpd/nanohttpd/blob/master/LICENSE.md");
                        Intent intent2 = new Intent();
                        intent2.setAction("android.intent.action.VIEW");
                        intent2.setData(uri2);
                        startActivity(intent2);
                        break;
                }
            }
        });
        builder.create().show();
    }

    public void LicenseAndroidAboutPage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LicensePage.this);
        builder.setTitle("请选择并查看相关开源信息");
        builder.setItems(about_license_AndroidAboutPage,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Uri uri1 = Uri.parse("https://github.com/medyo/android-about-page");
                        Intent intent1 = new Intent();
                        intent1.setAction("android.intent.action.VIEW");
                        intent1.setData(uri1);
                        startActivity(intent1);
                        break;
                    case 1:
                        Uri uri2 = Uri.parse("https://mit-license.org/");
                        Intent intent2 = new Intent();
                        intent2.setAction("android.intent.action.VIEW");
                        intent2.setData(uri2);
                        startActivity(intent2);
                        break;
                }
            }
        });
        builder.create().show();
    }

    public void LicenseRetrofit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LicensePage.this);
        builder.setTitle("请选择并查看相关开源信息");
        builder.setItems(about_license_Retrofit,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Uri uri1 = Uri.parse("https://github.com/square/retrofit");
                        Intent intent1 = new Intent();
                        intent1.setAction("android.intent.action.VIEW");
                        intent1.setData(uri1);
                        startActivity(intent1);
                        break;
                    case 1:
                        Uri uri2 = Uri.parse("https://github.com/square/retrofit/blob/master/LICENSE.txt");
                        Intent intent2 = new Intent();
                        intent2.setAction("android.intent.action.VIEW");
                        intent2.setData(uri2);
                        startActivity(intent2);
                        break;
                }
            }
        });
        builder.create().show();
    }
    public void LicenseZXingLite() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LicensePage.this);
        builder.setTitle("请选择并查看相关开源信息");
        builder.setItems(about_license_ZXingLite,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Uri uri1 = Uri.parse("https://github.com/jenly1314/ZXingLite");
                        Intent intent1 = new Intent();
                        intent1.setAction("android.intent.action.VIEW");
                        intent1.setData(uri1);
                        startActivity(intent1);
                        break;
                    case 1:
                        Uri uri2 = Uri.parse("https://www.apache.org/licenses/LICENSE-2.0");
                        Intent intent2 = new Intent();
                        intent2.setAction("android.intent.action.VIEW");
                        intent2.setData(uri2);
                        startActivity(intent2);
                        break;
                }
            }
        });
        builder.create().show();
    }
    public void LicenseAndroid_neumorphic() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LicensePage.this);
        builder.setTitle("请选择并查看相关开源信息");
        builder.setItems(about_license_Android_neumorphic,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Uri uri1 = Uri.parse("https://github.com/sshadkany/Android_neumorphic");
                        Intent intent1 = new Intent();
                        intent1.setAction("android.intent.action.VIEW");
                        intent1.setData(uri1);
                        startActivity(intent1);
                        break;
                    case 1:
                        Uri uri2 = Uri.parse("https://github.com/sshadkany/Android_neumorphic");
                        Intent intent2 = new Intent();
                        intent2.setAction("android.intent.action.VIEW");
                        intent2.setData(uri2);
                        startActivity(intent2);
                        break;
                }
            }
        });
        builder.create().show();
    }
}