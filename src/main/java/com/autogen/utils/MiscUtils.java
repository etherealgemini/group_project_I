package com.autogen.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MiscUtils {
    private MiscUtils(){}

    /**
     * 获取当前时间
     * @return
     *      当前日期，格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String getNow(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}
