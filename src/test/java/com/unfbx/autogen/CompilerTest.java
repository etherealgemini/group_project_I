package com.unfbx.autogen;

import com.autogen.service.IOService;
import org.junit.Test;

import static cn.hutool.core.compiler.CompilerUtil.compile;

public class CompilerTest {

    @Test
    public void CompilerTest(){
        IOService service = new IOService();
        String root = "D:\\Coding\\Creative\\group_project_I\\data";
        compile(root,root+"\\lib",root+"\\tests",root+"\\TargetTester.java","TargetTester.java");
    }
}
