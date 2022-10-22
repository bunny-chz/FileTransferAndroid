package com.bunny.FileTransferLite.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;

/**
 * Project:  文件快传
 * Comments: Uri转路径工具类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-16
 * Version: 1.0
 */



public class GetPathByUri {

    public static String GetPathByUri(Context context, Intent intent)throws Exception {
        return getPath(context, intent.getData());
    }
    /**
     * 通过Uri获取路径
     *
     * @param context 上下文
     * @param uri 系统的Uri
     */
    public static String getPath(Context context, Uri uri) throws Exception {
        String realPath = null;
        //如果大于4.4
        if (isKitKat()) {
            //如果是document类型uri, 则通过id获取
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if (isDownloadsDocuments(uri)) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    String[] proj = {"_data"};
                    Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        realPath = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                        cursor.close();
                    }

                } else if (isMediaDocuments(uri)) {
                    String id = docId.split(":")[1];
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://media/external/file"), Long.valueOf(id));
                    realPath = getRealPath(context, contentUri, null);
					/*
					 String type = docId.split(":")[0];
					 String id = docId.split(":")[1];
					 Uri contentUri = null;
					 String selection = "_id=" + id;
					 if (type.equals("image")) {
					 contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					 } else if (type.equals("audio")) {
					 contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					 } else if (type.equals("video")) {
					 contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					 }
					 if (contentUri != null && selection != null) {
					 //realPath = getRealPath(context, contentUri, selection);
					 realPath=getRealPath(context,contentUri,null);
					 }
					 */
                } else if (isExternalStorageDocuments(uri)) {
                    return getRootPath() + "/" + docId.split(":")[1];
                }

            } else if (isSchemeContent(uri)) {
                if (isRE(uri) || isEstrongs(uri)) {
                    realPath = uri.getPath();
                } else if (isQQBrowserFileProvider(uri)) {
                    realPath = getRootPath() + uri.getPath();
                } else if (isFileExplorerMyProvider(uri)) {
                    realPath = uri.getPath().replaceFirst("/external_files", getRootPath());
                } else {
                    realPath = getRealPath(context, uri, null);
                }
            } else if (isSchemeFile(uri)) {
                realPath = uri.getPath();
            }
        } else {
            //小于4.4
            realPath = getRealPath(context, uri, null);
        }
        return realPath;
    }



    @SuppressLint("Range")
    private static String getRealPath(Context context, Uri uri, String selection) {

        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex("_data"));
            cursor.close();
        }
        return path;
    }


    private static boolean isRE(Uri uri) {
        ///storage/emulated/0/log.txt
        // content
        return uri.getAuthority().equals("com.speedsoftware.rootexplorer.content");
    }
    private static boolean isEstrongs(Uri uri) {
        ///storage/emulated/0/log.txt
        // content
        return uri.getAuthority().equals("com.estrongs.files");
    }

    private static boolean isMIUIGallery(Uri uri) {
        // /raw//storage/emulated/0/DCIM/Camera/IMG_20200318_080535.jpg
        // content
        return uri.getAuthority().equals("com.miui.gallery.open");
    }
    private static boolean isMedia(Uri uri) {
        // /external/audio/media/69767
        // /external/images/media/86837
        // /external/file/130685
        // content
        return uri.getAuthority().equals("media");
    }
    private static boolean isQQBrowserFileProvider(Uri uri) {
        // /QQBrowser/log.txt
        // content
        return uri.getAuthority().equals("com.tencent.mtt.fileprovider");
    }
    private static boolean isFileExplorerMyProvider(Uri uri) {
        // /external_files/netease/cloudmusic/Music/许嵩 - 幻听.mp3
        // content
        return uri.getAuthority().equals("com.android.fileexplorer.myprovider");
    }
    private static boolean isDownloadsDocuments(Uri uri) {
        // /document/503
        // documentUri
        return uri.getAuthority().equals("com.android.providers.downloads.documents");
    }
    private static boolean isMediaDocuments(Uri uri) {
        // /document/audio:69767
        // /document/video:126419
        // /document/image:130682
        // documentUri
        return uri.getAuthority().equals("com.android.providers.media.documents");
    }
    private static boolean isExternalStorageDocuments(Uri uri) {
        // /document/primary:{文件相对路径}
        // documentUri
        return uri.getAuthority().equals("com.android.externalstorage.documents");
    }

    private static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
    private static boolean isSchemeContent(Uri uri) {
        return uri.getScheme().equals(ContentResolver.SCHEME_CONTENT);
    }

    private static boolean isSchemeFile(Uri uri) {
        return uri.getScheme().equals(ContentResolver.SCHEME_FILE);
    }

    private static String getRootPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }
}
