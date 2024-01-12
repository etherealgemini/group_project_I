package com.autogen.utils;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

public class FileUtils {

    public static File[] getClassesFiles(String clazzPathStr){
        File clazzPath = new File(clazzPathStr);
        if (clazzPath.exists() && clazzPath.isDirectory()) {
            // 获取路径长度
            Deque<File> stack = new ArrayDeque<>();
            stack.push(clazzPath);

            // 遍历类路径
            return getClassesFiles(stack);
        }
        return null;
    }
    @Nullable
    public static File[] getClassesFiles(Deque<File> stack) {
        File path = stack.pop();
        File[] classFiles = path.listFiles(pathname -> {
            //只加载class文件
            return pathname.isDirectory() || pathname.getName().endsWith(".class");
        });
        if (classFiles == null) {
            return null;
        }
        return classFiles;
    }
}
