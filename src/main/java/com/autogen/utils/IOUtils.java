package com.autogen.utils;

import com.autogen.model.Code;
import com.autogen.model.Ignore;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.autogen.utils.CompileUtils.compile;

@Slf4j
public class IOUtils {

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
    public static Code writeTestFileToJavaFile(String test, String path) throws IOException {
        if(writeTestFileToJavaFile(test, path, null)!=null){
            return Code.SUCCESS;
        }else{
            return Code.EVALUATION_IO_WRITING_ERROR;
        }
    }
    public static Code writeTestFileToJavaFile(String test, String path,String programRootPath, String compileDest,String compileLib) {

        File javaFile = writeTestFileToJavaFile(test, path, null);
        if(javaFile==null){
            return Code.EVALUATION_IO_WRITING_ERROR;
        }

        log.info("Compiling {}...", javaFile.getAbsolutePath());
        compile(programRootPath,
                compileLib,
                compileDest,
                javaFile.getAbsolutePath()
        );
        return Code.SUCCESS;
    }
    private static File writeTestFileToJavaFile(String test, String path, Ignore ignore){
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
            log.info("Test file written to {}", javaFile.getAbsolutePath());
            System.out.println("文件创建成功！");
            osw.close();
            return javaFile;
        } catch (IOException e) {
            log.error("Compile error: ",e);
            return null;
        }
    }

    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine())!=null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
    public static Code writeFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            return Code.SUCCESS;
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            return Code.EVALUATION_IO_WRITING_ERROR;
        }
    }
    public static String getFileName(String filePath){
        return filePath.substring(filePath.lastIndexOf(File.separator)+1);
    }


    public static Pattern keyMatchLimitCount = Pattern.compile("\\$\\{([a-zA-Z]+)}");
    /**
     * A convenient getString() extension with "${param}" replacement.
     * @param resource
     * @param k
     * @return
     */
    public static String getPropertiesString(ResourceBundle resource, String k){
        String v = resource.getString(k);
        Matcher m = keyMatchLimitCount.matcher(v);
        while (m.find()) {
//            String param = m.group(1);
            v = m.replaceAll(resource
                    .getString(m.group(1))
                    .replace("\\","\\\\"));
        }
        return v;
    }
}
