

package com.device.hk.common;


public class CommonUtil {

    //SDK时间解析
    public static String parseTime(int time) {
        int year = (time >> 26) + 2000;
        int month = (time >> 22) & 15;
        int day = (time >> 17) & 31;
        int hour = (time >> 12) & 31;
        int min = (time >> 6) & 63;
        int second = (time >> 0) & 63;
        String sTime = year + "-" + month + "-" + day + "-" + hour + ":" + min + ":" + second;
//        System.out.println(sTime);
        return sTime;


    }

    public static String getLibPath() {
        try {
            String path = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
//            path = path.replace("/classes/", "/hk-device-lib/");
            path = path.replace("/classes/", "/hk-lib/");
            path = path.replace("/target/", "/");
            path = path.substring(1, path.length());
            path = path.replace("/", "\\");
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLocalIP() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
