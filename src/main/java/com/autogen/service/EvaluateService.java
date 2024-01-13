package com.autogen.service;

import com.autogen.model.Code;

import java.util.HashMap;
import java.util.Map;

public class EvaluateService {

    private final CoverageTester coverageTester = new CoverageTester(System.out,new HashMap<>());
    public void evaluateTest(int type,String path) throws Exception {
        switch (type){
            case 0:
                coverageTester.execute("\\data\\targets","\\data\\tests");
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

    public Code analyseResult(int type) {
        switch (type){
            case 0:{
                Map<String,Double> result = coverageTester.getResultMap();
                if(result.isEmpty()){
                    return Code.EVALUATION_ANALYZE_ERROR;
                }
                if(result.get("lines")<-1.0){
                    return Code.WORSE_COVERAGE_LINE;
                }
                return Code.EVALUATION_PASS;
            }
            default:
                break;
        }
        return Code.EVALUATION_ANALYZE_ERROR;
    }

}
