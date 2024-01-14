package com.autogen.service;

import com.autogen.model.Code;
import com.autogen.utils.Formatter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.autogen.utils.IOUtils.writeTestFileToJavaFile;

@Slf4j
public class EvaluationService {
    private static Map<String,Double> coverageThresholds;
    private static Map<String,Double> mutationResults;

    /**
     * Compiled program path, used by system.
     */
    private static String targetPath;
    /**
     * Compiled tests path, used by system.
     */
    private static String testPath;
    private static String rootPath;
    private static String libPath;
    private static String programRootPath;

    private final static EvaluationService EVALUATION_SERVICE = new EvaluationService();


    private EvaluationService(){}
    public static EvaluationService getInstance(){
        return EVALUATION_SERVICE;
    }
    public static EvaluationService getInstance(String programRootPath, String targetPath,String testPath,
                                                String rootPath,String libPath){
        EvaluationService.programRootPath = programRootPath;
        EvaluationService.rootPath = rootPath;
        EvaluationService.targetPath = targetPath;
        EvaluationService.testPath = testPath;
        EvaluationService.libPath = libPath;
        return EVALUATION_SERVICE;
    }

    /**
     * 可以直接将gpt的回复放进来，请放入content
     * @param raw
     * @return
     */
    public Code evaluateTestFromGPT(String raw){
        String code = Formatter.codeBlockFormatter(raw,"java");

        Code cpResult = writeTestFileToJavaFile(code,rootPath,programRootPath,testPath,libPath);
        System.out.println("Io and compile result: "+cpResult);

        try {
            evaluateTest(0, targetPath,testPath);
        } catch (Exception e){
            log.error(e.getMessage());
            return Code.EVALUATION_ERROR;
        }
        try {
            return analyseResult(0);
        } catch (Exception e){
            log.error(e.getMessage());
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
                log.info("Running coverage test on test files at {}, the target file are at {}",
                        testPath,targetPath);
                coverageTester.execute(targetPath,testPath);
                break;
            case 1:
                evaluateTestMutation(targetPath,testPath);
                break;
            case 100:
                log.info("Running coverage test on test files at {}, the target file are at {}",
                        testPath,targetPath);
                log.info("The result will be cloned as baseline.");

                coverageTester.execute(targetPath,testPath);
                coverageThresholds = coverageTester.cloneResultMap();

                log.info("The baseline is: {}",coverageThresholds);
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
