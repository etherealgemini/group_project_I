package com.autogen.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class IOService {

    public int writeTestFileToJavaFile(String test,String path,boolean needFormat) throws IOException {
        if (needFormat)
            test = test.substring(4, test.length() - 3);

        FileOutputStream fos;
        OutputStreamWriter osw = null;
        try {
            File file = new File(path);
            fos = new FileOutputStream(file);
            System.out.println("文件创建成功！");
            osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            osw.write(test);
            osw.flush();
            return 1;
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            return -1;
        } finally {
            if (osw != null) osw.close();
        }
    }
}
