package com.autogen.utils;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.autogen.utils.IOUtils.getFileName;

@Slf4j
public class CompileUtils {
    /**
     * A runtime compile method.
     * @param rootPath
     *      the target's root directory, used in classpath configuration
     * @param jarPath
     *      the third-party jar dependencies' directory
     * @param dest
     *      where the compiled class files are stored.
     * @param filePath
     *      if the path is to the file, then compile that file;<br>
     *      if the path is to the directory, then compile all the .java files in that directory.<br>
     *      must be the subdirectory of rootPath!
     * @return
     *      whether compiled successfully.
     */
    public static boolean compile(String rootPath, String jarPath, String dest, String filePath){

        //Do not want to dump all .class files into destination,
        //will extract the target file to dest later
        List<String> options = new ArrayList<>(List.of("-d", "."));
        List<String> jars;


        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null);

        File javaFile = new File(filePath);

        Iterable<? extends JavaFileObject> files;
        List<File> javaFiles = null;
        boolean targetIsFile = javaFile.isFile();

        String dirFilePath = targetIsFile?filePath.substring(0,filePath.lastIndexOf(".")):filePath;

        if (jarPath!=null){
            log.info("Adding third-party jar dependencies' in directory: {}", jarPath);
            jars = classPathConfig(new ArrayList<>(List.of(rootPath,dirFilePath)),jarPath);
            options.addAll(jars);
        }

        if(javaFile.exists()){
            if(targetIsFile){
                files = fileManager.getJavaFileObjectsFromStrings(
                                Collections.singletonList(filePath));
            } else {
                javaFiles = List.of(Objects.requireNonNull(javaFile.listFiles((dir, name) -> name.endsWith(".java"))));
                files = fileManager.getJavaFileObjectsFromFiles(javaFiles);
            }
        }else {
            return false;
        }
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, null, options, null, files);
        Boolean result = task.call();
        if (result) {
            if(targetIsFile){
                File classFile = new File(filePath.replace(".java", ".class"));
                if(classFile.exists()){
                    FileUtil.move(classFile, new File(
                                    dest+"\\"+getFileName(classFile.getAbsolutePath()).replace(".java", ".class")),
                            true);
                }
            }else{
                javaFiles.forEach(file ->{
                    String classFile = file.getName().replace(".java", ".class");
                    String classFileName = getFileName(classFile);
                    FileUtil.touch(new File(dest + "\\" + classFileName));
                    FileUtil.move(new File(classFile), new File(dest + "\\" + classFileName),true);
                });
            }

            log.info("Compile {} successfully", filePath);
        }
        return result;
    }


    /**
     * Automatically add the third-party jars in the specified path
     * to the classpath configuration.
     *
     * @param paths
     * @param jarPath
     * @return
     */
    private static List<String> classPathConfig(ArrayList<String> paths,String jarPath){
        File p = new File(jarPath);
        StringBuilder s = new StringBuilder();
        List<String> list = new ArrayList<>();
        list.add("-cp");
        if (p.exists()&&p.isDirectory()){
            File[] jars = p.listFiles(((dir,name) -> name.endsWith(".jar")));
            if(jars==null){
                return list;
            }
            for (File jar : jars) {
                s.append(";").append(jar.getAbsolutePath());
            }
        }
        paths.forEach(path -> s.append(";").append(path));
        s.append(";.;");
        list.add(s.toString());
        return list;
    }
}
