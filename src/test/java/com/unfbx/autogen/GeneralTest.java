package com.unfbx.autogen;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.autogen.utils.CommandLineUtils.run_cmd;
import static com.autogen.utils.CommandLineUtils.run_cmd_example;
import static com.autogen.utils.PDFParser.parsePDFtoString;

public class GeneralTest {
    private static ResourceBundle autogen;

    @Before
    public void init(){
        //1. 从资源文件读入各类路径
        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());
    }

    @Test
    public void PDFTest(){
        String pdf = parsePDFtoString(autogen.getString("pdfInputPath"));
        Assert.assertNotNull(pdf);
        System.out.println(pdf);
//        System.out.println(pdf);
    }

    @Test
    public void cmdTest() {
        run_cmd_example();
    }
}
