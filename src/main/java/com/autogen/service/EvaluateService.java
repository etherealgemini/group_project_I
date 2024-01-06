package com.autogen.service;

import com.autogen.utils.CoverageTestReporter;
import jdk.jshell.spi.ExecutionControl;

import java.io.File;

public class EvaluateService {

    public void evaluateTest(int type,String path){
        switch (type){
            case 0:
                CoverageTestReporter covTest = new CoverageTestReporter(new File(path), new File("./src/main/java"),
                        new File(path+"/execData"), new File(path+"/report"));
                covTest.test(path);
                break;
            case 1:
                evaluateTestMutation(path);
                break;
            default:
                break;
        }
    }
    private void evaluateTestMutation(String path){
        try{
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] analyseResult(String path) {
        try{
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
