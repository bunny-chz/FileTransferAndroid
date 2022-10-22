package com.bunny.FileTransferLite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bunny.FileTransferLite.utils.NetWorkUtils;
import com.bunny.FileTransferLite.utils.SaveData;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bunny.FileTransferLite.sshadkany.neo;
import com.bunny.FileTransferLite.sshadkany.RectButton;

/**
 * Project:  文件快传
 * Comments: MainActivity类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-02
 * Version: 1.0
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity_LOG_TAG";
    SaveData saveData = new SaveData(this);
    TextView ipText;
    int port = 9102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();
//        if (Build.VERSION.SDK_INT >= 30) {
//
//        }
        // Android11以上需要申请所有文件访问管理权限，否则服务器无法保存上传上来的文件
        @SuppressLint({"NewApi", "LocalSuppress"}) boolean highPermission = Environment.isExternalStorageManager();
        if (!highPermission) {
            @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.fromParts("package", this.getPackageName(), null));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }
        initView();
        initWifi();
        StartThreadUpdateUI();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        RectButton moreBtn = findViewById(R.id.moreBtn);
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,moreBtn);
                popupMenu.getMenuInflater().inflate(R.menu.about_page,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Intent i1 = new Intent(MainActivity.this,MyAboutPage.class);
                        startActivity(i1);
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        final neo serverBtn = findViewById(R.id.serverBtn);
        ViewGroup viewGroupServer = findViewById(R.id.serverBtn);
        final ImageView imageViewServerBtn = (ImageView) viewGroupServer.getChildAt(0);
        serverBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // is shape Contains Point ----> for detect place of Touch is in the shape or not
                if (serverBtn.isShapeContainsPoint(event.getX(), event.getY())) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // PRESSED
                            //use only "small inner shadow" because its same size with "drop shadow" style and "big inner shadow" is bigger
                            // "small inner shadow" = "drop shadow"
                            // "big inner shadow"  > "drop shadow"
                            serverBtn.setStyle(neo.small_inner_shadow);
                            imageViewServerBtn.setScaleX(imageViewServerBtn.getScaleX() * 0.9f);
                            imageViewServerBtn.setScaleY(imageViewServerBtn.getScaleY() * 0.9f);
                            Intent i1 = new Intent(MainActivity.this,ServerActivity.class);
                            startActivity(i1);
                            return true; // if you want to handle the touch event
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            // RELEASED
                            serverBtn.setStyle(neo.drop_shadow);
                            imageViewServerBtn.setScaleX(1);
                            imageViewServerBtn.setScaleY(1);
                            return true; // if you want to handle the touch event
                    }
                }
                return false;
            }
        });
        final neo clientBtn = findViewById(R.id.clientBtn);
        ViewGroup viewGroupClient = findViewById(R.id.clientBtn);
        final ImageView imageViewClientBtn = (ImageView) viewGroupClient.getChildAt(0);
        clientBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // is shape Contains Point ----> for detect place of Touch is in the shape or not
                if (clientBtn.isShapeContainsPoint(event.getX(), event.getY())) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // PRESSED
                            //use only "small inner shadow" because its same size with "drop shadow" style and "big inner shadow" is bigger
                            // "small inner shadow" = "drop shadow"
                            // "big inner shadow"  > "drop shadow"
                            clientBtn.setStyle(neo.small_inner_shadow);
                            imageViewClientBtn.setScaleX(imageViewClientBtn.getScaleX() * 0.9f);
                            imageViewClientBtn.setScaleY(imageViewClientBtn.getScaleY() * 0.9f);
                            Intent i1 = new Intent(MainActivity.this,ClientActivity.class);
                            startActivity(i1);
                            return true; // if you want to handle the touch event
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            // RELEASED
                            clientBtn.setStyle(neo.drop_shadow);
                            imageViewClientBtn.setScaleX(1);
                            imageViewClientBtn.setScaleY(1);
                            return true; // if you want to handle the touch event
                    }
                }
                return false;
            }
        });
        ipText = findViewById(R.id.ipText);
        initData();
    }
    /**
     * 初始化数据
     */
    void initData() {
        initPort();
        String portStr = String.valueOf(port);
        String ipString = NetWorkUtils.getLocalIpAddress(this);
        ipText.setText("IP地址: " + ipString);
        ipText.setBackground(getDrawable(R.drawable.wifi_on));
        if(ipString.contains("0.0.0.0")){
            ipText.setText("请连接WiFi重启,让操作的设备处于同一个局域网");
            ipText.setBackground(getDrawable(R.drawable.wifi_off));
        }
        saveData.saveString(ipString + ":" + portStr,"HostPort");
    }

    /**
     * 初始化WiFi，Lite版本不用用户设置那么多参数，打开即可使用，自动设置好服务器主机端口
     */
    public void initWifi() {
        WifiManager mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (mWifiManager.isWifiEnabled() || ipAddress != 0) {
            Log.d(TAG,"WiFi已经打开 ---> ");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            builder.setTitle("提示");
            builder.setMessage("请连接WiFi后重新打开本软件,让操作的设备处于同一个局域网，否则无法使用！");
            builder.setPositiveButton("关闭本软件", (dialogInterface, i) -> {
                finish();
            });
            builder.setNegativeButton("去连接WiFi", (dialogInterface, i) -> {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                finish();
            });
            builder.create().show();
        }
    }

    /**
     *  for循环检测可用端口，15次
     */
    public void initPort() {
        for (int i = port; i < 9117; i++){
            if(!isPortAvailable(port)){
                Log.d(TAG,"可用端口: port ---> " + port);
                break;
            }
            else{
                Log.d(TAG,"不可用端口: port ---> " + port);
            }
        }
    }
    /**
     * 检测端口是否被占用
     */
    private void bindPort(String host,int port)throws Exception{
        //创建一个socket对象
        Socket s = new Socket();
        //对指定端口进行绑定，如果绑定成功则未被占用
        s.bind(new InetSocketAddress(host,port));
        s.close();
    }
    public boolean isPortAvailable(int port){
        try{
            //调用bindport函数对本机指定端口进行验证
            bindPort("0.0.0.0",port);
            bindPort(InetAddress.getLocalHost().getHostAddress(),port);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 开启一个子线程，更新网络状态
     */
    private void StartThreadUpdateUI() {
        new Thread(){
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(3000);
                        Message message=new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }while (true);
            }
        }.start();
    }

    /**
     * 在主线程中进行数据处理
     */
    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler(){
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                initPort();
                String ipString = NetWorkUtils.getLocalIpAddress(MainActivity.this);
                String tmpStr = saveData.loadString("HostPort");
                String[] strList = tmpStr.split(":");
                ipText.setText("IP地址: " + ipString);
                ipText.setBackground(getDrawable(R.drawable.wifi_on));
                if(ipString.contains("0.0.0.0")){
                    ipText.setText("请连接WiFi重启,让操作的设备处于同一个局域网");
                    ipText.setBackground(getDrawable(R.drawable.wifi_off));
                }
            }
        }
    };

    /**
     * 获取当前时间 yyyy/MM/dd HH:mm:ss
     *
     * @return 当前时间 yyyy/MM/dd HH:mm:ss
     */
    public String currentTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //制定输出格式
        Date d = new Date();
        return simpleDateFormat.format(d);
    }
    /**
     * 设置右上角菜单布局文件
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_page,menu);
        return true;
    }
    /**
     * 设置右上角菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about_page) {
            Intent i1 = new Intent(MainActivity.this,MyAboutPage.class);
            startActivity(i1);
        }
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        @SuppressLint("InlinedApi")
        String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        ArrayList<String> toApplyList = new ArrayList<>();
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);// 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 0x01);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     *再按一次退出主界面操作
     **/
    long exitTime = 0;
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
            return;
        }
        finish();
    }
}