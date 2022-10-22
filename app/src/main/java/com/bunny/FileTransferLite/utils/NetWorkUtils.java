package com.bunny.FileTransferLite.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Project:  文件快传
 * Comments: 获取网络IP工具类
 * JDK version used: <JDK1.8>
 * Author： Bunny     Github: https://github.com/bunny-chz/
 * Create Date：2022-10-16
 * Version: 1.0
 */


public class NetWorkUtils {
        /**
         * 将ip的整数形式转换成ip形式
         *
         * @param ipInt
         * @return
         */
        public static String int2ip(int ipInt) {
            StringBuilder sb = new StringBuilder();
            sb.append(ipInt & 0xFF).append(".");
            sb.append((ipInt >> 8) & 0xFF).append(".");
            sb.append((ipInt >> 16) & 0xFF).append(".");
            sb.append((ipInt >> 24) & 0xFF);
            return sb.toString();
        }

        /**
         * 获取当前ip地址
         *
         * @param context
         * @return
         */
        public static String getLocalIpAddress(Context context) {
            try {

                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int i = wifiInfo.getIpAddress();
                return int2ip(i);
            } catch (Exception ex) {
                return " 获取IP出错!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
            }
            //return null;
        }
}
