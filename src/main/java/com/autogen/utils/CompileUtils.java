package com.autogen.utils;

import cn.hutool.core.io.FileUtil;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompileUtils {
    public static boolean compile(String rootPath, String jarPath, String dest, String filePath,String fileName){
        File root = new File(rootPath);

        List<String> jars = thirdPartyJarsAutoConfig(root.getAbsolutePath(),jarPath);
        List<String> options = new ArrayList<>(List.of("-d", "."));
        options.addAll(jars);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> files =
                fileManager.getJavaFileObjectsFromStrings(
                        Collections.singletonList(filePath));
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, null, options, null, files);
        Boolean result = task.call();
        if (result) {
            File classFile = new File(filePath.replace(".java", ".class"));
            if(classFile.exists()){
                FileUtil.move(classFile, new File(
                                dest+"\\"+fileName.replace(".java", ".class")),
                        true);
            }
            System.out.println("Succeeded");
        }
        return result;
    }


    /**
     * Automatically add the third-party jars in the specified path
     * to the classpath configuration.
     * @param path
     * @return
     */
    private static List<String> thirdPartyJarsAutoConfig(String root,String path){
        File p = new File(path);
        StringBuilder s = new StringBuilder();
        List<String> list = new ArrayList<>();
        list.add("-cp");
        if (p.exists()&&p.isDirectory()){
//            FilenameFilter
            File[] jars = p.listFiles(((dir,name) -> name.endsWith(".jar")));
            if(jars==null){
                return list;
            }
            for (File jar : jars) {
                s.append(";").append(jar.getAbsolutePath());
            }
        }
        s.append(";").append(root).append(";");
        list.add(s.toString());
        return list;
    }
}
