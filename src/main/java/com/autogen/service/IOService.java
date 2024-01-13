package com.autogen.service;

import cn.hutool.core.io.FileUtil;
import com.autogen.model.Code;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IOService {

    public Code writeTestFileToJavaFile(String test, String path, boolean needFormat,boolean compile) throws IOException {
        if (needFormat)
            test = test.substring(4, test.length() - 3);


        FileOutputStream fos;
        OutputStreamWriter osw = null;
        try {
            File file = new File(path);
            File javaFile = file;
            String className = null;
            if(file.exists()&&file.isDirectory()){
                int classNameIdx0 = test.indexOf("public class");
                int classNameIdx1 = test.indexOf("{");
                className = test.substring(classNameIdx0 + 13, classNameIdx1-1);
                javaFile = new File(path+"\\"+className+".java");
                fos = new FileOutputStream(javaFile);
            }
            else fos = new FileOutputStream(file);

            osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            osw.write(test);
            osw.flush();
            System.out.println("文件创建成功！");
            osw.close();
            if (compile) {
//                run_cmd("javac *.java -verbose -d"+ path + "\\tests" +" -cp .\\lib\\*;");
                compile(path,path+"\\lib",path+"\\tests",javaFile.getAbsolutePath(),className);
//                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//
//                String argz = "-verbose -cp *;"+" -d "+path+"\\tests";
//                InputStream in = new ByteArrayInputStream(argz.getBytes());
//
//                int result = compiler.run(in, null, null, file.getPath());
//                if (result != 0) {
//                    return Code.EVALUATION_COMPILE_ERROR;
//                }
            }
            return Code.SUCCESS;
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            return Code.EVALUATION_IO_WRITING_ERROR;
        } finally {
            if (osw != null) osw.close();
        }
    }

    public boolean compile(String rootPath, String jarPath, String dest, String filePath,String fileName){
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
    private List<String> thirdPartyJarsAutoConfig(String root,String path){
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
