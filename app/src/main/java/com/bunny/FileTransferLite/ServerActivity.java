package com.bunny.FileTransferLite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bunny.FileTransferLite.utils.GetPathByUri;
import com.bunny.FileTransferLite.utils.NetWorkUtils;
import com.bunny.FileTransferLite.utils.SaveData;
import com.bunny.FileTransferLite.utils.TextUtil;
import com.king.zxing.util.CodeUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.view.ViewGroup;
import com.bunny.FileTransferLite.sshadkany.neo;
import com.bunny.FileTransferLite.sshadkany.RectButton;
/**
 * Project:  文件快传
 * Comments: 服务器界面类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-02
 * Version: 1.0
 */
public class ServerActivity extends AppCompatActivity {

    public static final String TAG = "NanoHTTP_LOG_TAG";
    public static final String IP = "0.0.0.0";
    private static final String SHARE_TEXT = "连接时请确保双方的设备都在同一个局域网内！！！\n\n" +
            "你的好朋友给你分享的服务器地址为:\n\n";

    SaveData saveData = new SaveData(this);

    Button severSwitchBtn, clearSendText, sendTextBtn, clearReceiveText, receiveTextBtn, selectFileToClientBtn, uploadFileLogBtn;
    EditText serverToClientEt, clientToServerEt;
    TextView selectFilePath, receiveTextTime, ipText, statusText;

    String toClientFilePath = null;
    String uploadFileLogStr = null;
    int port = 9102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_page);
        initServer();
        StartThreadUpdateUI();
    }

    /**
     * 初始化AndroidWebServer类构造
     */
    public void initServer(){
        if (saveData.loadString("HostPort") == null){
            AndroidWebServer androidWebServer = new AndroidWebServer(IP,9102);
            initView(androidWebServer);
        }
        if (saveData.loadString("HostPort") != null){
            String tmpStr = saveData.loadString("HostPort");
            String[] strList = tmpStr.split(":");
            String host = strList[0];
            int port = Integer.parseInt(strList[1]);
            Log.d(TAG,"host ---> " + host);
            Log.d(TAG,"port ---> " + port);
            AndroidWebServer androidWebServer = new AndroidWebServer(host,port);
            initView(androidWebServer);
        }
    }
    /**
     * 初始化控件函数
     */
    private void initView(AndroidWebServer androidWebServer){
        RectButton backBtn = findViewById(R.id.backBtn);
        RectButton moreBtn = findViewById(R.id.moreBtn);
        RectButton clearSendBtn = findViewById(R.id.clearSendBtn);
        RectButton sendBtn = findViewById(R.id.sendBtn);
        RectButton clearReceiveBtn = findViewById(R.id.clearReceiveBtn);
        RectButton receiveBtn = findViewById(R.id.receiveBtn);
        RectButton checkUploadBtn = findViewById(R.id.checkUploadBtn);
        RectButton sendFileBtn = findViewById(R.id.sendFileBtn);
        serverToClientEt = findViewById(R.id.serverToClientEt);
        clientToServerEt = findViewById(R.id.clientToServerEt);
        receiveTextTime = findViewById(R.id.receiveTextTime);
        selectFilePath = findViewById(R.id.selectFilePath);
        statusText = findViewById(R.id.statusText);
        ipText = findViewById(R.id.ipText);
        initPort();
        String portStr = String.valueOf(port);
        String ipString = NetWorkUtils.getLocalIpAddress(ServerActivity.this);
        ipText.setText("IP地址: " + ipString + " 端口: " + portStr);
        ipText.setBackground(getDrawable(R.drawable.wifi_on));
        if(ipString.contains("0.0.0.0")){
            ipText.setText("请连接WiFi重启,让操作的设备处于同一个局域网");
            ipText.setBackground(getDrawable(R.drawable.wifi_off));
        }
        final neo severSwitchBtn = findViewById(R.id.severSwitchBtn);
        ViewGroup viewGroupseverSwitchBtn = findViewById(R.id.severSwitchBtn);
        final ImageView imageViewSeverSwitchBtn = (ImageView) viewGroupseverSwitchBtn.getChildAt(0);
        severSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"severStatusBtn CLICK --->");
                if (severSwitchBtn.style == neo.drop_shadow){
                    try {
                        androidWebServer.start();
                        severSwitchBtn.setStyle(neo.small_inner_shadow);
                        imageViewSeverSwitchBtn.setImageTintList(getResources().getColorStateList(R.color.BtnOn));
                        imageViewSeverSwitchBtn.setScaleX(imageViewSeverSwitchBtn.getScaleX() * 0.9f);
                        imageViewSeverSwitchBtn.setScaleY(imageViewSeverSwitchBtn.getScaleY() * 0.9f);
                        statusText.setText("服务器已开启");
                        statusText.setTextColor(getResources().getColor(R.color.BtnOn));
                        Log.d(TAG,"Server OPEN --->");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (severSwitchBtn.style == neo.small_inner_shadow){
                    Log.d(TAG,"Server CLOSE --->");
                    androidWebServer.stop();
                    severSwitchBtn.setStyle(neo.drop_shadow);
                    imageViewSeverSwitchBtn.setImageTintList(getResources().getColorStateList(R.color.BtnOff));
                    imageViewSeverSwitchBtn.setScaleX(1);
                    imageViewSeverSwitchBtn.setScaleY(1);
                    statusText.setText("服务器已关闭");
                    statusText.setTextColor(getResources().getColor(R.color.BtnOff));
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"moreBtn Click ---> ");
                PopupMenu popupMenu = new PopupMenu(ServerActivity.this,moreBtn);
                popupMenu.getMenuInflater().inflate(R.menu.server_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.menu_generateCode) {
                            try {
                                String textContent = "http://" + saveData.loadString("HostPort");
                                if (TextUtils.isEmpty(textContent)) {
                                    Toast.makeText(ServerActivity.this, "输入为空，请您输入字符！", Toast.LENGTH_SHORT).show();
                                }
                                Bitmap bitmap = CodeUtils.createQRCode(textContent, 600, null);//生成高度为600的二维码
                                generateCodeView(bitmap,textContent);
                            } catch (Exception e) {
                                Toast.makeText(ServerActivity.this, "错误：无法生成二维码", Toast.LENGTH_SHORT).show();
                            }
                            Log.d(TAG,"generateCode ---> ");
                        }
                        if (menuItem.getItemId() == R.id.menu_shareServer) {
                            String textContent = "http://" + saveData.loadString("HostPort");
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, SHARE_TEXT + textContent);
                            intent.setType("text/plain");
                            startActivity(intent);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        clearSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverToClientEt.setText("");
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"  sendTextBtn CLICK ---> ");
                if(!TextUtils.isEmpty(serverToClientEt.getText().toString())){
                    String sendStr = "时间: " + currentTime() + "#-#-#" + serverToClientEt.getText().toString();
                    File file = new File(Environment.getExternalStorageDirectory() + "/文件快传/AppConfig/TextStoC.txt");
                    Log.d(TAG,"  Start Create File ---> ");
                    if (!file.getParentFile().getParentFile().mkdir()){
                        Log.d(TAG,"  Create Dir ---> ");
                        file.getParentFile().getParentFile().mkdir();
                    }
                    if (!file.getParentFile().mkdir()){
                        Log.d(TAG,"  Create Dir ---> ");
                        file.getParentFile().mkdir();
                    }
                    try {
                        Log.d(TAG,"  Start Write ---> ");
                        write(sendStr,file);
                        Log.d(TAG,"  Write Over ---> ");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG,"  错误 ---> ");
                    }
                }else {
                    Toast.makeText(ServerActivity.this, "空文本！请输入内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
        clearReceiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientToServerEt.setText("");
                receiveTextTime.setText("");
            }
        });
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = androidWebServer.getText();
                if(!str.equals("")){
                    String[] tmpList = str.split("#-#-#");
                    String textStr = tmpList[1];
                    String timeStr = tmpList[0];
                    receiveTextTime.setText("时间: " + timeStr);
                    Log.d(TAG,"  decode TEXT ---> " + textStr);
                    try {
                        String text =  URLDecoder.decode(textStr,"utf-8");
                        Log.d(TAG," After decoded TEXT ---> " + text);
                        clientToServerEt.setText(text);
                        clientToServerEt.setSelection(text.length());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(ServerActivity.this, "客户端没有发送信息", Toast.LENGTH_SHORT).show();
                }
            }
        });
        receiveBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(clientToServerEt.getText().toString().equals("")){
                    Toast.makeText(ServerActivity.this, "接收输入框的文本为空，无法大屏编辑", Toast.LENGTH_SHORT).show();
                }else {
                    fullScreenEdit(clientToServerEt.getText().toString());
                }
                return false;
            }
        });
        checkUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFileLogStr = androidWebServer.getUploadFileLog();
                AlertDialog.Builder builder = new AlertDialog.Builder(ServerActivity.this);
                builder.setCancelable(true);
                builder.setTitle("上传文件日志(软件关闭自动删除)");
                if(uploadFileLogStr.equals("")){
                    builder.setMessage("无上传记录");
                }
                else {
                    builder.setMessage(uploadFileLogStr);
                }
                builder.setPositiveButton("确定", null);
                builder.create().show();
            }
        });
        sendFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 0x01);
            }
        });
    }

    /**
     * 用RandomAccessFile类写配置信息
     *
     * @param content
     * @param file
     * @throws IOException
     */
    public void write(String content, File file) throws IOException {
        if(file.exists()){
            file.delete();
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(content.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String mimeType = "text/plain";
            ContentResolver resolver = this.getContentResolver();
            mimeType = resolver.getType(uri);
            Log.d(TAG,"Uri ---> " + uri);
            Log.d(TAG," mimeType ---> " + mimeType);
            File file = new File(Environment.getExternalStorageDirectory() + "/文件快传/AppConfig/FileStoC.txt");
            if (!file.getParentFile().getParentFile().mkdir()){
                Log.d(TAG,"  Create Dir ---> ");
                file.getParentFile().getParentFile().mkdir();
            }
            if (!file.getParentFile().mkdir()){
                Log.d(TAG,"  Create Dir ---> ");
                file.getParentFile().mkdir();
            }
            try {
                toClientFilePath = GetPathByUri.getPath(ServerActivity.this, uri);
                Log.d(TAG," File ---> " + toClientFilePath);
                selectFilePath.setText("已选择文件的路径为:\n" + toClientFilePath);
                String configText = toClientFilePath + "#-#-#" + mimeType;
                write(configText,file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成二维码对话框
     *
     * @param bitmap 生成的二维码bitmap
     * @param str 此时的服务器主机端口Http网址
     */
    public void generateCodeView(Bitmap bitmap,String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ServerActivity.this);
        final AlertDialog dialog = builder.create();
        LayoutInflater factory = LayoutInflater.from(ServerActivity.this);
        final View view = factory.inflate(R.layout.generate_code_view,null);
        dialog.setTitle("");
        dialog.setView(view);
        dialog.setCancelable(true);
        dialog.show();
        Button bt_confirm = view.findViewById(R.id.bt_confirm);
        Button bt_copy = view.findViewById(R.id.bt_copy);
        ImageView generateCodeImage = view.findViewById(R.id.generateCodeImage);
        TextView scanResult = view.findViewById(R.id.scanResult);
        generateCodeImage.setImageBitmap(bitmap);
        scanResult.setText(str);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        bt_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("url", str);
                clipboard.setPrimaryClip(clip);
                dialog.dismiss();
                Toast.makeText(ServerActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 自定义大屏编辑对话框
     *
     * @param str 原来在接收输入框的文本，转到了大屏编辑输入框中
     */
    public void fullScreenEdit(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ServerActivity.this);
        final AlertDialog dialog = builder.create();
        LayoutInflater factory = LayoutInflater.from(ServerActivity.this);
        final View view = factory.inflate(R.layout.full_screen_edit,null);
        dialog.setTitle("大屏编辑");
        dialog.setView(view);
        dialog.setCancelable(false);
        dialog.show();
        Button copy = view.findViewById(R.id.copy);
        Button close = view.findViewById(R.id.close);
        Button pick_link = view.findViewById(R.id.pick_link);
        EditText dialog_edit = view.findViewById(R.id.dialog_edit);
        dialog_edit.setText(str);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(ServerActivity.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", dialog_edit.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ServerActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        pick_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String url = TextUtil.getUrl(str);
                if(!url.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ServerActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("提取结果");
                    builder.setMessage(url);
                    builder.setPositiveButton("确定", null);
                    builder.setNegativeButton("复制到剪切板", (dialogInterface, i) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(ServerActivity.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("url", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(ServerActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                    });
                    builder.setNeutralButton("浏览器访问", (dialogInterface, i) -> {
                        try {
                            Uri uri = Uri.parse(url);
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setData(uri);
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(ServerActivity.this, "错误：网络链接格式有误或者没安装浏览器", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();
                }
                if(url.equals("")) {
                    Toast.makeText(ServerActivity.this, "无网络链接可提取", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                String portStr = String.valueOf(port);
                String ipString = NetWorkUtils.getLocalIpAddress(ServerActivity.this);
                ipText.setText("IP地址: " + ipString + " 端口: " + portStr);
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
}
