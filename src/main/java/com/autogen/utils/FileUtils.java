package com.autogen.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Slf4j
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

    public static String[] getClassesNames(String clazzPathStr){
        File[] files = getClassesFiles(clazzPathStr);
        return getClassesNames(files);
    }

    public static String[] getClassesNames(File[] files){
        String[] s = null;
        if (files != null) {
            s = new String[files.length];
            for (int i = 0; i< files.length;i++) {
                File file = files[i];
                String t = file.getName();
                s[i] = t.substring(0, t.length() - 6);
            }
        }

        return s;
    }

    public static byte[] getFileByte(File file){
        if(file.exists()){
            byte[] bytes = new byte[0];
            try {
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                bytes = baos.toByteArray();
                fis.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info(String.format("Load file %s as byte array success",file.getName()));
            return bytes;
        }else{
            return null;
        }
    }

    /**
     * Load all files as bytes into a hashmap.
     * @param rootPath
     *      The class path.
     * @param postfix
     *      e.g., ".class", ".java", ".txt"
     *
     * @return
     *       A hashmap of file name and its byte array.
     */
   public static HashMap<String,byte[]> getFileByte(String rootPath, String postfix){
        File rootPath_ = new File(rootPath);
        HashMap<String, byte[]> fileByteMap = new HashMap<>();
        Deque<File> stack = new ArrayDeque<>();
        stack.push(rootPath_);

        if(rootPath_.exists() && rootPath_.isDirectory()){
            while(!stack.isEmpty()){
                rootPath_ = stack.pop();
                int rootPathLen = rootPath_.getAbsolutePath().length() + 1;
                File[] files = rootPath_.listFiles(pathname -> {
                    //只加载class文件
                    return pathname.isDirectory() || pathname.getName().endsWith(postfix);
                });
                if(files ==null)
                    return fileByteMap;

                for(File file : files){
                    if(file.isDirectory()){
                        stack.push(file);
                        continue;
                    }
                    String rawName = file.getAbsolutePath();
                    String filename = rawName.substring(rootPathLen, rawName.length() - postfix.length());
                    filename = filename.replace(File.separatorChar, '.');

                    byte[] bytes = getFileByte(file);

                    fileByteMap.put(filename,bytes);
//                    addOriDefinition(className,bytes,isTest);
                }
            }
        }
        return fileByteMap;
    }

    public static ArrayList<URL> loadJarFiles(ArrayList<URL> urls, File file) throws MalformedURLException {
//        ArrayList<URL> urls = new ArrayList<>();
//        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
//        System.out.println("load Classpath of dir : " + file.getAbsolutePath());
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    loadJarFiles(urls,subFile);
                }
            }
        } else {
            if (file.getAbsolutePath().endsWith(".jar")) {
                urls.add(file.toURI().toURL());
            }
        }
        return urls;
    }

    public static void writeByte(byte[] bytes, String path) {
       File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A simple method to remove all sub files under a directory.
     * @param path
     *      The path to a directory. If it is a file, it will be deleted directly.
     * @param all
     *      If true, all files and sub-directories will be deleted.
     *      If false, only the files under the directory will be deleted.
     */
    public static void cleanUp(String path,boolean all) {
        log.info("Delete files under {}",path);
       File file = new File(path);
       if(file.isFile()){
           file.delete();
           return;
       }
       for(File subFile : Objects.requireNonNull(file.listFiles())){
           if(subFile.isDirectory()) {
               cleanUp(subFile.getAbsolutePath(), all);
               if(all){
                   subFile.deleteOnExit();
               }
           }else{
               subFile.deleteOnExit();
           }
       }
    }
}
