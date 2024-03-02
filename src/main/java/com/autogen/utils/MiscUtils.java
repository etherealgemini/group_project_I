package com.autogen.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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

    /**
     *
     * @param arr
     *      e.g., ["a","b","c"]
     * @param sep
     *      e.g., "+"
     * @return
     *      e.g., "a+b+c"
     */
    public static String arrayToString(String[] arr,String sep){
        StringBuilder s = new StringBuilder();
        for (String ss:arr){
            s.append(ss).append(sep);
        }
        s.deleteCharAt(s.length()-1);
        return s.toString();
    }

    /**
     * Tested 2024.03.02
     * @param str xxxx98%xxxx
     * @return 0.98
     */
    public static ArrayList<String> findNum(String str){
        String pat = "\\d+";
        Pattern pattern = Pattern.compile(pat);
        Matcher matcher = pattern.matcher(str);

        ArrayList<String> o = new ArrayList<>();

        while(matcher.find()){
            o.add(matcher.group());
        }

        return o;
    }

    /**
     * This method is designed just for a String:Double map.
     * @param map
     * @return
     */
    public static Map<String,Double> cloneHashMap(Map<String,Double> map){
        HashMap<String,Double> newMap = new HashMap<>();
        try{
            for(String k:map.keySet()){
                newMap.put(k, Double.parseDouble(String.valueOf(map.get(k))));
            }
        }catch (NumberFormatException e){
            log.error("Error occurred when clone a map.",e);
        }

        return newMap;
    }
}
