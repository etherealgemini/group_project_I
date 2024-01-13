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

import static cn.hutool.core.compiler.CompilerUtil.compile;


public class IOService {

    /**
     *
     * @param test
     *      The string content of test file.
     * @param path
     *      The output path of test file.
     * @param compile
     *      If true then the file will be compiled after written.
     * @return
     * @throws IOException
     */
    public Code writeTestFileToJavaFile(String test, String path, boolean compile) throws IOException {
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
                compile(path,
                        path+"\\lib",
                        path+"\\tests",
                        javaFile.getAbsolutePath(),
                        className);
            }
            return Code.SUCCESS;
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            return Code.EVALUATION_IO_WRITING_ERROR;
        } finally {
            if (osw != null) osw.close();
        }
    }


}
