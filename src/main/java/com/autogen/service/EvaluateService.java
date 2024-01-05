package com.autogen.service;

import com.autogen.utils.CoverageTest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;

public class EvaluateService {

    public void evaluateTest(int type,String path){
        switch (type){
            case 0:
                CoverageTest covTest = new CoverageTest(new File(path), new File("./src/main/java"),
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
        throw new NotImplementedException();
    }

    public String[] analyseResult(String path) {
        throw new NotImplementedException();
    }
}
