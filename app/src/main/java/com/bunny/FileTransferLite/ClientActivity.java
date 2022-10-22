package com.bunny.FileTransferLite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bunny.FileTransferLite.sshadkany.RectButton;
import com.bunny.FileTransferLite.utils.GetPathByUri;
import com.bunny.FileTransferLite.utils.SaveData;
import com.bunny.FileTransferLite.utils.TextUtil;
import com.king.zxing.CameraScan;
import com.king.zxing.CaptureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Project:  文件快传
 * Comments: 客户端界面类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-20
 * Version: 1.0
 */

public class ClientActivity extends AppCompatActivity {
    public static final String TAG = "Retrofit_LOG_TAG";
    public static final int REQUEST_CODE_SCAN = 0X02;



    EditText stringEditText, serverToClientEt;
    TextView filePathText, status, receiveTextTime, baseUrl;
    ImageView img_loading;

    SaveData saveData = new SaveData(this);
    String filePath = null;
    String downloadLogStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_page);
        //初始化控件
        initView();
    }

    /**
     * 初始化控件函数
     */
    private void initView(){
        RectButton backBtn = findViewById(R.id.backBtn);
        RectButton moreBtn = findViewById(R.id.moreBtn);
        RectButton clearTextBtn = findViewById(R.id.clearTextBtn);
        RectButton postStringBtn = findViewById(R.id.postStringBtn);
        RectButton selectFile = findViewById(R.id.selectFile);
        RectButton sendFile = findViewById(R.id.sendFile);
        RectButton downloadFileFromServer = findViewById(R.id.downloadFileFromServer);
        RectButton checkDownloadLog = findViewById(R.id.checkDownloadLog);
        RectButton testLink = findViewById(R.id.testLink);
        RectButton receiveTextBtn = findViewById(R.id.receiveTextBtn);
        RectButton clearReceiveTextBtn = findViewById(R.id.clearReceiveTextBtn);
        filePathText = findViewById(R.id.filePathText);
        stringEditText = findViewById(R.id.stringEditText);
        img_loading = findViewById(R.id.img_loading);
        status = findViewById(R.id.status);
        serverToClientEt = findViewById(R.id.serverToClientEt);
        receiveTextTime = findViewById(R.id.receiveTextTime);
        baseUrl = findViewById(R.id.baseUrl);
        baseUrl.setText("当前服务器地址为空，请点击右上角按钮设置");
        //读取服务器主机端口
        if (saveData.loadString("BaseUrl") != null){
            baseUrl.setText("当前服务器地址: " + saveData.loadString("BaseUrl"));
        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ClientActivity.this,moreBtn);
                popupMenu.getMenuInflater().inflate(R.menu.client_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.menu_editHostPort) {
                            editBaseUrl();
                        }
                        if (menuItem.getItemId() == R.id.menu_scanQRCode) {
                            startActivityForResult(new Intent(ClientActivity.this, CaptureActivity.class),REQUEST_CODE_SCAN,null);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        //清空发送输入框
        clearTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stringEditText.setText("");
                saveData.saveString("","POST_TEXT");
            }
        });
        //连接测试
        testLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断是否设置主机端口
                if (saveData.loadString("BaseUrl") == null){
                    Toast.makeText(ClientActivity.this, "网络地址为空，请点击右上角按钮设置", Toast.LENGTH_SHORT).show();
                } else {
                    //按下加载标志旋转
                    Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                    operatingAnim.setInterpolator(new LinearInterpolator());
                    img_loading.startAnimation(operatingAnim);
                    img_loading.setVisibility(View.VISIBLE);
                    status.setText("连接测试中...");
                    String baseUrl = saveData.loadString("BaseUrl");
                    HttpInterface httpInterface = RetrofitCreator.getInstance(baseUrl).getRetrofit().create(HttpInterface.class);
                    Call<ResponseBody> task = httpInterface.testLink();
                    task.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            int code = response.code();
                            Log.d(TAG,"Response_code ---> " + code);
                            if (code == HttpURLConnection.HTTP_OK){
                                try {
                                    Log.d(TAG,"post_response_body ---> " + response.body());
                                    //加载图标停止旋转
                                    Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                                    operatingAnim.setInterpolator(new LinearInterpolator());
                                    img_loading.clearAnimation();
                                    img_loading.setVisibility(View.INVISIBLE);
                                    status.setText("");
                                    Toast.makeText(ClientActivity.this, "连接成功，服务器状态正常", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.d(TAG,"Get Fail ---> " + t.getStackTrace());
                            //加载图标停止旋转
                            Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                            operatingAnim.setInterpolator(new LinearInterpolator());
                            img_loading.clearAnimation();
                            img_loading.setVisibility(View.INVISIBLE);
                            status.setText("");
                            Toast.makeText(ClientActivity.this, "连接失败，服务器状态异常\n\n" +
                                    t.getStackTrace(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        //发送文本给服务器
        postStringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveData.loadString("BaseUrl") == null){
                    Toast.makeText(ClientActivity.this, "网络地址为空，请点击右上角按钮设置", Toast.LENGTH_SHORT).show();
                } else {
                    String informContent = stringEditText.getText().toString();
                    if (informContent.isEmpty()){
                        Toast.makeText(ClientActivity.this, "发送的内容为空，请输入！", Toast.LENGTH_SHORT).show();
                    }else{
                        Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                        operatingAnim.setInterpolator(new LinearInterpolator());
                        img_loading.startAnimation(operatingAnim);
                        img_loading.setVisibility(View.VISIBLE);
                        status.setText("发送文字中...");
                        String baseUrl = saveData.loadString("BaseUrl");
                        HttpInterface httpInterface = RetrofitCreator.getInstance(baseUrl).getRetrofit().create(HttpInterface.class);
                        Call<ResponseBody> task = httpInterface.postString(informContent);
                        task.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                int code = response.code();
                                Log.d(TAG,"Response_code ---> " + code);
                                if (code == HttpURLConnection.HTTP_OK){
                                    try {
                                        Log.d(TAG,"post_response_body ---> " + response.body());
                                        Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                                        operatingAnim.setInterpolator(new LinearInterpolator());
                                        img_loading.clearAnimation();
                                        img_loading.setVisibility(View.INVISIBLE);
                                        status.setText("");
                                        Toast.makeText(ClientActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                        saveData.saveString(informContent,"POST_TEXT");
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d(TAG,"Get Fail ---> " + t.getStackTrace());
                                Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                                operatingAnim.setInterpolator(new LinearInterpolator());
                                img_loading.clearAnimation();
                                img_loading.setVisibility(View.INVISIBLE);
                                status.setText("");
                                Toast.makeText(ClientActivity.this, "发送失败\n\n" +
                                        t.getStackTrace(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }}
            }
        });
        //清空接收文本输入框
        clearReceiveTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverToClientEt.setText("");
                receiveTextTime.setText("");
            }
        });
        //接收服务器发送的文本，点击事件
        receiveTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveData.loadString("BaseUrl") == null){
                    Toast.makeText(ClientActivity.this, "网络地址为空，请点击右上角按钮设置", Toast.LENGTH_SHORT).show();
                } else {
                    Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                    operatingAnim.setInterpolator(new LinearInterpolator());
                    img_loading.startAnimation(operatingAnim);
                    img_loading.setVisibility(View.VISIBLE);
                    status.setText("接收文字中...");
                    String baseUrl = saveData.loadString("BaseUrl");
                    HttpInterface httpInterface = RetrofitCreator.getInstance(baseUrl).getRetrofit().create(HttpInterface.class);
                    Call<ResponseBody> task = httpInterface.getText();
                    task.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            int code = response.code();
                            Log.d(TAG,"Response_code ---> " + code);
                            if (code == HttpURLConnection.HTTP_OK){
                                try {
                                    String str = response.body().string();
                                    Log.d(TAG,"str ---> " + str);
                                    if (!str.equals("")){
                                        String[] tmpStrs = str.split("#-#-#");
                                        String textTime = tmpStrs[0];
                                        Log.d(TAG,"textTime ---> " + textTime);
                                        String textContent = tmpStrs[1];
                                        Log.d(TAG,"textContent ---> " + textContent);
                                        receiveTextTime.setText(textTime);
                                        serverToClientEt.setText(textContent);
                                        serverToClientEt.setSelection(textContent.length());
                                        Toast.makeText(ClientActivity.this, "接收成功，服务器状态正常", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(ClientActivity.this, "服务器没有发送信息", Toast.LENGTH_SHORT).show();
                                    }
                                    Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                                    operatingAnim.setInterpolator(new LinearInterpolator());
                                    img_loading.clearAnimation();
                                    img_loading.setVisibility(View.INVISIBLE);
                                    status.setText("");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.d(TAG,"Get Fail ---> " + t.getStackTrace());
                            Toast.makeText(ClientActivity.this, "接收失败，请检查服务器连接\n\n" +
                                    t.getStackTrace(), Toast.LENGTH_SHORT).show();
                            Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                            operatingAnim.setInterpolator(new LinearInterpolator());
                            img_loading.clearAnimation();
                            img_loading.setVisibility(View.INVISIBLE);
                            status.setText("");
                        }
                    });
                }
            }
        });
        //长按进入大屏编辑
        receiveTextBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(serverToClientEt.getText().toString().equals("")){
                    Toast.makeText(ClientActivity.this, "接收输入框的文本为空，无法大屏编辑", Toast.LENGTH_SHORT).show();
                }else {
                    fullScreenEdit(serverToClientEt.getText().toString());
                }
                return false;
            }
        });
        //点击下载服务器分享的文件
        downloadFileFromServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveData.loadString("BaseUrl") == null){
                    Toast.makeText(ClientActivity.this, "网络地址为空，请点击右上角按钮设置", Toast.LENGTH_SHORT).show();
                } else {
                    Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                    operatingAnim.setInterpolator(new LinearInterpolator());
                    img_loading.startAnimation(operatingAnim);
                    img_loading.setVisibility(View.VISIBLE);
                    status.setText("下载文件中...");
                    String baseUrl = saveData.loadString("BaseUrl");
                    HttpInterface httpInterface = RetrofitCreator.getInstance(baseUrl).getRetrofit().create(HttpInterface.class);
                    Call<ResponseBody> task = httpInterface.downFile();
                    task.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            int code = response.code();
                            Log.d(TAG,"Response_code ---> " + code);
                            if (code == HttpURLConnection.HTTP_OK){
                                Headers headers = response.headers();
                                for(int i = 0; i < headers.size(); i++) {
                                    Log.d(TAG,headers.name(i) + " == " + headers.value(i));
                                }
                                writeFile2Sd(response,headers);
                                Toast.makeText(ClientActivity.this, "下载成功，服务器状态正常", Toast.LENGTH_SHORT).show();
                                Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                                operatingAnim.setInterpolator(new LinearInterpolator());
                                img_loading.clearAnimation();
                                img_loading.setVisibility(View.INVISIBLE);
                                status.setText("");
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.d(TAG,"Get Fail ---> " + t.getStackTrace());
                            Toast.makeText(ClientActivity.this, "下载失败，请检查服务器连接\n\n" +
                                    t.getStackTrace(), Toast.LENGTH_SHORT).show();
                            Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                            operatingAnim.setInterpolator(new LinearInterpolator());
                            img_loading.clearAnimation();
                            img_loading.setVisibility(View.INVISIBLE);
                            status.setText("");
                        }
                    });
                }
            }
        });
        //查看下载文件记录
        checkDownloadLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = getDownloadLogStr();
                AlertDialog.Builder builder = new AlertDialog.Builder(ClientActivity.this);
                builder.setCancelable(true);
                builder.setTitle("下载文件日志(软件关闭自动删除)");
                if(str.equals("")){
                    builder.setMessage("无上传记录");
                }
                else {
                    builder.setMessage(str);
                }
                builder.setPositiveButton("确定", null);
                builder.create().show();
            }
        });
        //打开文件管理器选择文件
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 0x01);
            }
        });
        //发送文件给服务器
        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveData.loadString("BaseUrl") == null){
                    Toast.makeText(ClientActivity.this, "网络地址为空，请点击右上角按钮设置", Toast.LENGTH_SHORT).show();
                } else {
                    if (filePath == null){
                        Toast.makeText(ClientActivity.this, "文件路径为空，请先选择文件", Toast.LENGTH_SHORT).show();
                    }else{
                        Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                        operatingAnim.setInterpolator(new LinearInterpolator());
                        img_loading.startAnimation(operatingAnim);
                        img_loading.setVisibility(View.VISIBLE);
                        status.setText("发送文件中...");
                        String baseUrl = saveData.loadString("BaseUrl");
                        HttpInterface httpInterface = RetrofitCreator.getInstance(baseUrl).getRetrofit().create(HttpInterface.class);
                        Map<String,Object> params = new HashMap<>();
                        MultipartBody.Part filePart = getPart(filePath,"file");
                        String[] splitStr =  filePath.split("/");
                        params.put("FileName",splitStr[splitStr.length - 1]);
                        Log.d(TAG,"Last splitStr ---> " + splitStr[splitStr.length - 1]);
                        Call<ResponseBody> task = httpInterface.postFile(params,filePart);
                        task.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                int code = response.code();
                                Log.d(TAG,"Response_code ---> " + code);
                                if (code == HttpURLConnection.HTTP_OK){
                                    try {
                                        Log.d(TAG,"postFile_response_body ---> " + response.body().toString());
                                        Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                                        operatingAnim.setInterpolator(new LinearInterpolator());
                                        img_loading.clearAnimation();
                                        img_loading.setVisibility(View.INVISIBLE);
                                        status.setText("");
                                        Toast.makeText(ClientActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d(TAG,"Get Fail ---> " + t.getStackTrace());
                                Animation operatingAnim = AnimationUtils.loadAnimation(ClientActivity.this, R.anim.rotate);
                                operatingAnim.setInterpolator(new LinearInterpolator());
                                img_loading.clearAnimation();
                                img_loading.setVisibility(View.INVISIBLE);
                                status.setText("");
                                Toast.makeText(ClientActivity.this, "发送失败\n\n" +
                                        t.getStackTrace(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }}
        });
    }

    /**
     * 从服务器下载的文件，保存到SD卡中
     *
     * @param response Retrofit 中服务器返回的响应
     * @param headers 服务器的响应头部
     */
    private void writeFile2Sd(final Response<ResponseBody> response,final Headers headers) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = headers.get("Content-Type");
                if(fileName != null) {
                    Log.d(TAG,"fileName -- > " + fileName);
                    File  file = new File(Environment.getExternalStorageDirectory() + "/文件快传/Download/" + fileName);
                    FileOutputStream fos = null;
                    try {
                        if (!file.getParentFile().getParentFile().mkdir()){
                            Log.d(TAG,"  Create Dir ---> ");
                            file.getParentFile().getParentFile().mkdir();
                        }
                        if (!file.getParentFile().mkdir()){
                            Log.d(TAG,"  Create Dir ---> ");
                            file.getParentFile().mkdir();
                        }
                        Log.i(TAG,"file.getAbsolutePath() ---> " + file.getAbsolutePath());
                        if(!file.exists()) {
                            file.createNewFile();
                        }
                        fos = new FileOutputStream(file);
                        InputStream inputStream = response.body().byteStream();
                        byte[] buf = new byte[1024];
                        int len;
                        while((len = inputStream.read(buf,0,buf.length)) != -1) {
                            fos.write(buf,0,len);
                        }
                        setDownloadLogStr(fileName);
                    } catch(Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(fos != null) {
                            try {
                                fos.close();
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 上传文件进行的MultipartBody.Part信息设置，
     *
     * @param path 文件路径
     * @param key 设置文件Body头部的键值
     * @return MultipartBody.Part
     */
    public MultipartBody.Part getPart(String path, String key){
        File file = new File(path);
        MediaType mediaType = MediaType.parse("image/jpg");
        RequestBody fileBody = RequestBody.create(mediaType,file);
        return MultipartBody.Part.createFormData(key,file.getName(),fileBody);
    }

    /**
     * 安卓onActivityResult，用于Activity界面跳转的数据传递
     *
     * @param requestCode 区分何种操作的数字
     * @param resultCode 操作结果数字
     * @param data 数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //点击 选择文件 按钮后进行的文件选择，得到文件的uri,然后转成绝对路径
        if (requestCode == 0x01 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.d(TAG,"Uri ---> " + uri);
            try {
                filePath = GetPathByUri.getPath(ClientActivity.this, uri);
                Log.d(TAG," File ---> " + filePath);
                filePathText.setText("已选择文件的路径为:\n" + filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //ZXingLite扫码得到结果后的操作
        else if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_CODE_SCAN) {
            String result = CameraScan.parseScanResult(data);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("扫描结果");
            builder.setMessage(result);
            builder.setPositiveButton("确定", null);
            builder.setNegativeButton("设置该服务器", (dialogInterface, i) -> {
                saveData.saveString(result,"BaseUrl");
                baseUrl.setText("当前服务器地址: " + saveData.loadString("BaseUrl"));
                Toast.makeText(ClientActivity.this, "设置服务器成功", Toast.LENGTH_SHORT).show();
            });
            builder.setNeutralButton("浏览器访问该服务器", (dialogInterface, i) -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("result", result);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                try {
                    Uri uri = Uri.parse(result);
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(uri);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(ClientActivity.this, "错误：网络链接有误或者没安装浏览器", Toast.LENGTH_SHORT).show();
                }
            });
            builder.create().show();
        }
    }

    /**
     * 自定义大屏编辑对话框
     *
     * @param str 原来在接收输入框的文本，转到了大屏编辑输入框中
     */
    public void fullScreenEdit(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClientActivity.this);
        final AlertDialog dialog = builder.create();
        LayoutInflater factory = LayoutInflater.from(ClientActivity.this);
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
                Toast.makeText(ClientActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ClientActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("提取结果");
                    builder.setMessage(url);
                    builder.setPositiveButton("确定", null);
                    builder.setNegativeButton("复制到剪切板", (dialogInterface, i) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(ServerActivity.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("url", url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(ClientActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                    });
                    builder.setNeutralButton("浏览器访问", (dialogInterface, i) -> {
                        try {
                            Uri uri = Uri.parse(url);
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setData(uri);
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(ClientActivity.this, "错误：网络链接格式有误或者没安装浏览器", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();
                }
                if(url.equals("")) {
                    Toast.makeText(ClientActivity.this, "无网络链接可提取", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 设置右上角菜单布局文件
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_menu,menu);
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
        if (item.getItemId() == R.id.menu_editHostPort) {
            editBaseUrl();
        }
        if (item.getItemId() == R.id.menu_scanQRCode) {
            startActivityForResult(new Intent(ClientActivity.this, CaptureActivity.class),REQUEST_CODE_SCAN,null);
        }
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 自定义编辑主机端口的对话框
     */
    public void editBaseUrl() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClientActivity.this);
        final AlertDialog dialog = builder.create();
        LayoutInflater factory = LayoutInflater.from(ClientActivity.this);
        final View view = factory.inflate(R.layout.client_edit_host_port,null);
        dialog.setTitle("设置HTTP服务器主机和端口");
        dialog.setView(view);
        dialog.setCancelable(true);
        dialog.show();
        Button confirm = view.findViewById(R.id.confirm);
        Button cancel = view.findViewById(R.id.cancel);
        Button clear = view.findViewById(R.id.clear);
        EditText dialog_edit = view.findViewById(R.id.dialog_edit);
        if(saveData.loadString("BaseUrl") != null) {
            dialog_edit.setText(saveData.loadString("BaseUrl").replace("http://",""));
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData.saveString(null,"BaseUrl");
                EditText dialog_edit = view.findViewById(R.id.dialog_edit);
                if(!TextUtils.isEmpty(dialog_edit.getText().toString())){
                    saveData.saveString("http://" + dialog_edit.getText().toString(),"BaseUrl");
                    baseUrl.setText("当前服务器地址: " + saveData.loadString("BaseUrl"));
                    Toast.makeText(ClientActivity.this, "编辑成功", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ClientActivity.this, "空文本！请输入内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_edit.setText("");
            }
        });
    }

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
     * 获取下载日志的记录字符串
     *
     * @return
     */
    public String getDownloadLogStr() {
        return downloadLogStr;
    }

    /**
     * 设置下载日志的记录字符串
     *
     * @param fileName
     */
    public void setDownloadLogStr(String fileName) {
        this.downloadLogStr = this.downloadLogStr + "\r\n" + "时间: " + currentTime() + "\r\n文件名: " + fileName;
    }
}
