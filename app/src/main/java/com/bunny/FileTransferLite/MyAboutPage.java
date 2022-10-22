package com.bunny.FileTransferLite;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Project:  文件快传
 * Comments: 关于界面类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-13
 * Version: 1.0
 */

public class MyAboutPage extends AppCompatActivity {
    private static final String SHARE_TEXT = "文件快传是一款支持文件和文本在手机间传输的软件。同时还支持在网页端操作。\n\n" +
            "下载时请看准安卓版本选择对应的下载！\n" +
            "蓝奏云下载：https://zss233.lanzout.com/b00q8e0hg\n" +
            "密码:2333\n";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.app_icon)
                .setDescription(this.getResources().getString(R.string.APP_description))
                .addItem(new Element().setTitle("Version 1.0.0"))
                .addItem(getShare())
                .addItem(getLicenseElement())
                .addGroup("联系开发者(Bunny)")
                .addGitHub("bunny-chz","GitHub(bunny-chz)")
                .create();
        setContentView(aboutPage);
    }
    Element getLicenseElement() {
        Element LicenseElement = new Element();
        LicenseElement.setTitle("版权信息");
        LicenseElement.setAutoApplyIconTint(true);
        LicenseElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        LicenseElement.setIconNightTint(android.R.color.white);
        LicenseElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MyAboutPage.this, LicensePage.class);
                startActivity(i);
            }
        });
        return LicenseElement;
    }
    Element getShare() {
        Element LicenseElement = new Element();
        LicenseElement.setTitle("分享");
        LicenseElement.setAutoApplyIconTint(true);
        LicenseElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        LicenseElement.setIconNightTint(android.R.color.white);
        LicenseElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, SHARE_TEXT);
                intent.setType("text/plain");
                startActivity(intent);
            }
        });
        return LicenseElement;
    }
}