package com.bunny.FileTransferLite;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import com.bunny.FileTransferLite.utils.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Project:  文件快传
 * Comments: NanoHttpD Server类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-14
 * Version: 1.0
 */

public class AndroidWebServer extends NanoHTTPD {
    public static final String TAG = "NanoHTTP_LOG_TAG";
    private String receiveText = "";
    private String uploadFileLog = "";

    /**
     * 构造函数，设置主机，端口
     *
     * @param hostname 主机名
     * @param port 端口号
     */
    public AndroidWebServer(String hostname, int port) {
        super(hostname, port);
    }

    /**
     * NanoHttpD Server重写函数，用于处理请求后，做出的响应
     *
     * @param session IHTTPSession 接口
     */
    @Override
    public Response serve(IHTTPSession session) {
        //获取客户端发起的URL，方便后面做判断
        String uri = session.getUri();
        //上传缓存文件名称路径的Map，上传文件操作记得加上
        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        //判断请求的方法和URL,分别对应到各个操作函数
        //GET获取主页
        if (Method.GET.equals(session.getMethod())  && uri.equals("/")) {
            return mainPageHTML();
        }
        //连接测试
        else if (Method.GET.equals(session.getMethod())  && uri.equals("/testLink")) {
            return  testLinkHTML();
        }
        //GET获取发送文本页面，textCtoS 即是 text from Client to Server的缩写
        else if (Method.GET.equals(session.getMethod())  && uri.equals("/textCtoSPage")){
            return textCToSHTML();
        }
        //POST文本到服务器， textCtoS 即是 text from Client to Server的缩写
        else if (Method.POST.equals(session.getMethod())  && uri.equals("/textCtoS")){
            return textCToS(session);
        }
        //手机客户端GET服务器发送的文本信息， textStoC 即是 text from Server to Client
        else if (Method.GET.equals(session.getMethod())  && uri.equals("/textStoC")){
            return getTextFromServer(false);
        }
        //网页端GET服务器发送的文本信息，textStoC 即是 text from Server to Client，Web是指网页端特定的URL
        else if (Method.GET.equals(session.getMethod())  && uri.equals("/textStoCWeb")){
            return getTextFromServer(true);
        }
        //判断请求的POST方法和URL，然后获取客户端上传的缓存文件路径，最后复制到手机服务器的某个路径
        //fileCtoS 即是 file from Client to Server的缩写
        else if (Method.POST.equals(session.getMethod())  && uri.equals("/fileCtoS")) {
            return responseUploadToServer(session, files);
        }
        //GET获取发送文件页面，fileCtoS 即是 file from Client to Server的缩写
        else if (Method.GET.equals(session.getMethod())  && uri.equals("/fileCtoSPage")) {
            return fileCToSHTML();
        }
        //网页端GET服务器发送的文件，fileStoC 即是 file from Server to Client，Web是指网页端特定的URL
        else if (Method.GET.equals(session.getMethod()) && uri.equals("/fileStoCWeb")){
            return getFileFromServer(true);
        }
        //手机客户端GET服务器发送的文件，fileStoC 即是 file from Server to Client
        else if (Method.GET.equals(session.getMethod()) && uri.equals("/fileStoC")){
            return getFileFromServer(false);
        }
        //404
        return responseNotFound();
    }

    /**
     * 处理客户端发送来的消息，并返回提示
     *
     * @param session IHTTPSession 接口
     * @return 发送文本给服务器操作成功提示页面
     */
    private Response textCToS(IHTTPSession session){
        //获取到post上来的参数 text=? 的键值对
        String tmpStr = session.getQueryParameterString();
        Log.d(TAG," tmpStr ---> " + tmpStr);
        //替换 "text=" 剩下的就是客户端发送的信息
        tmpStr = tmpStr.replace("text=","");
        Log.d(TAG," tmpStr ---> " + tmpStr);
        //set文本信息给本类的全局变量 receiveText
        setText(tmpStr);
        Log.d(TAG," receiveText ---> " + receiveText);
        //返回操作提示给客户端
        return operationSuccessHTML("发送文本");
    }

    /**
     * 接收来自客户端上传的文件，处理后复制到系统正常目录
     *
     * @param session IHTTPSession 接口
     * @param files
     * @return
     */
    private Response responseUploadToServer(IHTTPSession session, Map<String, String> files) {
        Log.i(TAG,"****************response UploadFile****************");
        //获取post方法的键值对信息
        Map<String, String> params = session.getParms();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramsKey = entry.getKey();
            Log.i(TAG, "paramsKey ---> " + paramsKey);
            //post头部信息的"file"
            if (paramsKey.contains("file")) {
                String tmpFilePath = files.get(paramsKey);
                //filename="文件名"
                String fileName = entry.getValue();
                Log.i(TAG, "tmpFilePath ---> " + tmpFilePath);
                Log.i(TAG,"fileName ---> " + fileName);
                //tmpFilePath 是NanoHTTPD上传文件的缓存路径
                File tmpFile = new File(tmpFilePath);
                //设置保存上传文件的正常手机路径
                File  targetFile = new File(Environment.getExternalStorageDirectory() + "/文件快传/Upload/" + fileName);
                //从Environment.getExternalStorageDirectory()开始判断文件夹是否创建，这里设置了2级 "/文件快传/Upload/" 判断2次
                if (!targetFile.getParentFile().getParentFile().mkdir()){
                    Log.d(TAG,"  Create Dir ---> ");
                    targetFile.getParentFile().getParentFile().mkdir();
                }
                if (!targetFile.getParentFile().mkdir()){
                    Log.d(TAG,"  Create Dir ---> ");
                    targetFile.getParentFile().mkdir();
                }
                Log.i(TAG,"tmpFile.getAbsolutePath() ---> " +  tmpFile.getAbsolutePath());
                Log.i(TAG,"targetFile.getAbsolutePath() ---> " + targetFile.getAbsolutePath());
                if(!tmpFile.exists()){
                    Log.i(TAG, "tmpFile not exists --->");
                }
                if(!tmpFile.isFile()){
                    Log.i(TAG, "tmpFile not isFile --->");
                }
                if(!tmpFile.canRead()){
                    Log.i(TAG, "tmpFile not canRead --->");
                }
                //开始复制缓存目录的文件到正常可见的存储目录
                FileUtil fileUtil =new FileUtil();
                //如果是安卓11以上，需要申请缓存文件的读取权限，否则复制不了，具体看MANAGE_EXTERNAL_STORAGE权限
                fileUtil.copyFile(tmpFile,targetFile);
                //set上传文件目录日志
                setUploadFileLog(fileName);
            }
        }
        //返回操作提示
        return operationSuccessHTML("上传文件");
    }

    /**
     *获取配置文件保存的文件路径，最后传到文件流 fileInputStream, 再把流给newFixedLengthResponse响应函数，客户端GET就会得到文件
     *
     * @param isWeb 是否是web网页端的请求，若否 newFixedLengthResponse函数 MimeType为文件名，弥补NanoHTTPD不能加进文件名的缺陷
     */
    public Response getFileFromServer(boolean isWeb) {
        //这里把服务器发给客户端的文件路径，保存在 FileStoC.txt文件里，GET请求时，读取返回给客户端
        File  getMethodReturnFile = new File(Environment.getExternalStorageDirectory() + "/文件快传/AppConfig/FileStoC.txt");
        //从Environment.getExternalStorageDirectory()开始判断文件夹是否创建，这里设置了2级 "/文件快传/Upload/" 判断2次
        if (!getMethodReturnFile.getParentFile().getParentFile().mkdir()){
            Log.d(TAG,"  Create Dir ---> ");
            getMethodReturnFile.getParentFile().getParentFile().mkdir();
        }
        if (!getMethodReturnFile.getParentFile().mkdir()){
            Log.d(TAG,"  Create Dir ---> ");
            getMethodReturnFile.getParentFile().mkdir();
        }
        if(!getMethodReturnFile.exists()){
            return responseGetNoneFileFromServer();
        }else {
            String getMethodReturnFilePath = null;
            String getMimeType = "text/plain";
            try {
                InputStream inputStream = new FileInputStream(getMethodReturnFile);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String content;
                while ((content = bufferedReader.readLine()) != null) {
                    sb.append(content + "");
                }
                //关流
                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
                if (sb.toString().equals("")){
                    responseGetNoneFileFromServer();
                }
                else {
                    String getConfigText = sb.toString();
                    // "#-#-#"用于分割文件名和MimeType的自定义字符串
                    String[] tmpList = getConfigText.split("#-#-#");
                    getMethodReturnFilePath = tmpList[0];
                    if (isWeb){
                        getMimeType = tmpList[1];
                    }else {
                        String[] tmpStrs = getMethodReturnFilePath.split("/");
                        getMimeType = tmpStrs[tmpStrs.length - 1];
                    }
                    Log.d(TAG," sb.toString() --->" + sb );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!getMethodReturnFilePath.equals("")){
                try {
                    FileInputStream fileInputStream = new FileInputStream(getMethodReturnFilePath);
                    try {
                        //NanoHttp 返回文件流函数
                        return newFixedLengthResponse(Response.Status.OK,getMimeType,fileInputStream,fileInputStream.available());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //返回服务器没有设置文件的提示
            return responseGetNoneFileFromServer();
        }
    }

    /**
     * 读取配置文件的文本信息
     *
     * @param getMethodReturnTextFile 保存要发送信息给客户端的文件路径
     * @return 读取到的保存的文本信息
     */
    public String getConfigParam(File getMethodReturnTextFile) {
        String str = "";
        try {
            InputStream inputStream = new FileInputStream(getMethodReturnTextFile);
            InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String length;
            while ((length = br.readLine()) != null) {
                sb.append(length + "");
            }
            //关流
            br.close();
            isr.close();
            inputStream.close();
            str = sb.toString();
            Log.d(TAG," sb.toString() --->" + sb );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 服务器响应 返回文件快传网页端首页导航页面
     *
     * @return 网页端首页HTML页面
     */
    public Response mainPageHTML() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>文件快传网页端首页</title>");
        builder.append("</head><body>");
        builder.append("<h1>文件快传网页端首页</h1>");
        builder.append("<ul><h2>上传文件到服务器</h2>");
        builder.append("<li><a href=\"./fileCtoSPage\">上传文件到服务器</a></li>");
        builder.append("<h2>从服务器下载文件</h2>");
        builder.append("<li><a href=\"./fileStoCWeb\">从服务器下载文件</a></li>");
        builder.append("<h2>发送文本给服务器</h2>");
        builder.append("<li><a href=\"./textCtoSPage\">发送文本给服务器</a></li>");
        builder.append("<h2>接收服务器的文本</h2>");
        builder.append("<li><a href=\"./textStoCWeb\">接收服务器的文本</a></li></ul>");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(Response.Status.OK,"text/html",builder.toString());
    }

    /**
     * 服务器响应 返回成功连接提示网页页面
     *
     * @return 连接成功提示HTML页面
     */
    public Response testLinkHTML() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>连接测试</title>");
        builder.append("</head><body>");
        builder.append("<h1>连接测试，收到此信息即是连接成功，状态正常！</h1>");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(Response.Status.OK,"text/html",builder.toString());
    }

    /**
     * 服务器响应 操作成功提示页面
     *
     * @param operation 操作的名称，如上传，发送
     * @return 操作成功HTML页面
     */
    public Response operationSuccessHTML(String operation) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>服务器提示</title>");
        builder.append("<h2>" + operation + " 操作成功！</h2>");
        builder.append("</body></html>\n");
        return NanoHTTPD.newFixedLengthResponse(Response.Status.OK,"text/html",builder.toString());
    }

    /**
     * 服务器响应 客户端给服务器发送文本的HTML页面
     *
     * @return 客户端给服务器发送文本的HTML页面
     */
    public Response textCToSHTML(){
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>发送文本</title>");
        builder.append("<h1>发送文本</h1>");
        builder.append("<form action=\"./textCtoS\" method=\"POST\">");
        builder.append("<h2>输入文本</h2>");
        builder.append("<input  type = \"text\" name=\"text\"/>");
        builder.append("<input type=\"submit\" value=\"发送\">");
        builder.append("</form>");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(builder.toString());
    }

    /**
     * 读取保存发送信息的文件，并返回给客户端
     *
     * @param isWeb 判断是否是网页端的请求
     * @return 发送给客户端的信息文本
     */
    public Response getTextFromServer(boolean isWeb) {
        String str = "";
        Log.d(TAG,"  开始读取 ---> ");
        File  getMethodReturnTextFile = new File(Environment.getExternalStorageDirectory() + "/文件快传/AppConfig/TextStoC.txt");
        if (!getMethodReturnTextFile.getParentFile().getParentFile().mkdir()){
            Log.d(TAG,"  Create Dir ---> ");
            getMethodReturnTextFile.getParentFile().getParentFile().mkdir();
        }
        if (!getMethodReturnTextFile.getParentFile().mkdir()){
            Log.d(TAG,"  Create Dir ---> ");
            getMethodReturnTextFile.getParentFile().mkdir();
        }
        if(!getMethodReturnTextFile.isFile()){
            Log.i(TAG, "getMethodReturnTextFile not isFile --->");
        }
        if(!getMethodReturnTextFile.canRead()){
            Log.i(TAG, "getMethodReturnTextFile not canRead --->");
        }
        if(!getMethodReturnTextFile.exists()){
            Log.d(TAG,"  提示无信息 ---> ");
            if (isWeb){
                return responseGetNoneTextFromServer();
            }else{
                return newFixedLengthResponse("");
            }
        }else {
            Log.d(TAG,"  存在并返回信息 ---> ");
            str = getConfigParam(getMethodReturnTextFile);
            //返回给手机客户端和电脑端
            if (!isWeb){
                return newFixedLengthResponse(Response.Status.OK,"text/plain",str);
            }
            //返回网页形式的结果给浏览器
            else {
                String[] tmpStrs = str.split("#-#-#");
                String textTime = tmpStrs[0];
                String textContent = tmpStrs[1];
                StringBuilder builder = new StringBuilder();
                builder.append("<!DOCTYPE html><html><body>");
                builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
                builder.append("<title>服务器发送的文本</title>");
                builder.append("<h1>服务器发送的文本</h1>");
                builder.append("<h2>" + textTime + "</h2>");
                builder.append("<h3>" + textContent + "</h3>");
                builder.append("</body></html>\n");
                return newFixedLengthResponse(Response.Status.OK,"text/html",builder.toString());
            }
        }
    }

    /**
     * 服务器响应404页面
     *
     * @return 404页面
     */
    public Response responseNotFound() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>404 Not Found</title>");
        builder.append("<h1>404 Not Found</h1>");
        builder.append("<h2>该站点不存在！</h2>");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(Response.Status.NOT_FOUND,"text/html",builder.toString());
    }

    /**
     * 服务器响应 上传文件操作页面
     *
     * @return 上传文件操作页面
     */

    public Response fileCToSHTML(){
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>上传文件</title>");
        builder.append("<h1>上传文件</h1>");
        builder.append("<form method=\"POST\" enctype=\"multipart/form-data\" action=\"./fileCtoS\">");
        builder.append("<input type=\"hidden\" name=\"upload\" value=\"uploadFile\">");
        builder.append("<h2>选择文件</h2>");
        builder.append("<input type=\"file\" name=\"file\"><br>");
        builder.append("<input type=\"submit\" value=\"上传文件\">");
        builder.append("</form>");
        builder.append("</body></html>\n");
        return NanoHTTPD.newFixedLengthResponse(Response.Status.OK,"text/html",builder.toString());
    }

    public Response responseGetNoneFileFromServer() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>服务器提示</title>");
        builder.append("<h2>服务器没有设置文件分享！</h2>");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(Response.Status.NOT_FOUND,"text/html",builder.toString());
    }

    public Response responseGetNoneTextFromServer() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("<title>服务器提示</title>");
        builder.append("<h2>服务器没有发送信息！</h2>");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(Response.Status.NOT_FOUND,"text/html",builder.toString());
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
     * set 客户端发送来的文本信息
     *
     * @param str 客户端发送来的文本信息
     */
    public void setText(String str){
        this.receiveText = currentTime() + "#-#-#" + str;
    }

    /**
     * get 客户端发送来的文本信息
     *
     * @return 客户端发送来的文本信息
     */
    public String getText(){
        return this.receiveText;
    }

    /**
     * get 客户端发送过来的文件记录Log
     *
     * @return 客户端发送过来的文件记录Log
     */
    public String getUploadFileLog() {
        return uploadFileLog;
    }

    /**
     * set 户端发送过来的文件记录Log
     *
     * @param uploadFileLog 户端发送过来的文件记录Log
     */
    public void setUploadFileLog(String uploadFileLog) {
        this.uploadFileLog = this.uploadFileLog + "\r\n" + "时间: " + currentTime() + "\r\n文件名: " + uploadFileLog;
    }
}
