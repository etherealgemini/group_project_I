package com.autogen.utils;

public class Formatter {
    public static String codeBlockFormatter(String raw,String type){
        String pat = "```"+type;
        int idx_1 = raw.indexOf(pat)+pat.length();
        int idx_2 = raw.lastIndexOf("```");
        return raw.substring(idx_1,idx_2);
    }
}
