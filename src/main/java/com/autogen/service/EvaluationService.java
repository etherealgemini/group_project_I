package com.autogen.service;

import com.autogen.model.Code;
import com.autogen.utils.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EvaluationService {
    public static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);

    private static Map<String,Double> coverageThresholds;
    private static Map<String,Double> mutationResults;
    private static String programPath = "data\\targets";
    private static String testPath;
    private static String tempTestPath = "data\\tests";
    IOService ioService = new IOService();

    private final static EvaluationService EVALUATION_SERVICE = new EvaluationService();
    private EvaluationService(){}
    public static EvaluationService getInstance(){
        return EVALUATION_SERVICE;
    }
    public static EvaluationService getInstance(String programPath,String testPath){
        EvaluationService.programPath = programPath;
        EvaluationService.testPath = testPath;
        return EVALUATION_SERVICE;
    }

    /**
     * 可以直接将gpt的回复放进来，请放入content
     * @param raw
     * @return
     */
    public Code evaluateTestFromGPT(String raw){
        String code = Formatter.codeBlockFormatter(raw,"java");

        try {
            Code cpResult = ioService.writeTestFileToJavaFile(code, tempTestPath,true);
            System.out.println("Io and compile result: "+cpResult);
        } catch (IOException e){
            logger.error("Cannot write test file string to java file!");
            return Code.EVALUATION_IO_WRITING_ERROR;
        }

        try {
            evaluateTest(0, programPath,tempTestPath);
        } catch (Exception e){
            logger.error(e.getMessage());
            return Code.EVALUATION_ERROR;
        }
        try {
            return analyseResult(0);
        } catch (Exception e){
            logger.error(e.getMessage());
            return Code.EVALUATION_ANALYZE_ERROR;
        }
    }

    private final CoverageTester coverageTester = new CoverageTester(System.out,new HashMap<>());

    /**
     *
     * @param type
     *      0: coverage test.<br>
     *      1: mutation test (Not implemented).
     * @param targetPath
     * @param testPath
     * @throws Exception
     */
    public void evaluateTest(int type, String targetPath, String testPath) throws Exception {
        switch (type){
            case 0:
                coverageTester.execute(targetPath,testPath);
                break;
            case 1:
                evaluateTestMutation(targetPath,testPath);
                break;
            case 100:
                coverageTester.execute(targetPath,testPath);
                coverageThresholds = coverageTester.cloneResultMap();
                break;
            default:
                break;
        }
    }
    private void evaluateTestMutation(String testPath, String targetPath){
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
