package com.unfbx.autogen;

import com.autogen.utils.CoverageTestReporter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CoverageTestTestReporter {

    @Test
    public void covTest(){
        String path = "D:\\Coding\\Creative\\src";
        CoverageTestReporter test = new CoverageTestReporter(path,path,path,path);
    }
}
