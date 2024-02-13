package com.autogen.service;

import com.autogen.model.Code;
import com.autogen.utils.FileUtils;
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
     * 可以直接将gpt的回复放进来，请放入content。
     * 方法已加锁，防止第一次用户测试与后续测试同步执行导致系统异常。
     * @param raw
     * @return
     */
    public synchronized Code evaluateTestFromGPT(String raw,HashMap<String,String> systemProperties){
        String code = Formatter.codeBlockFormatter(raw,"java");

        // Better not to reconstruct this method.
        Code cpResult = writeTestFileToJavaFile(code,rootPath,programRootPath,testPath,libPath);
        System.out.println("Io and compile result: "+cpResult);

        try {
            evaluateTest(0, systemProperties);
        } catch (Exception e){
            log.error(e.getMessage());
            return Code.EVALUATION_ERROR;
        }
        try {
            Code result = analyseResult(0);

            //clean up to prepare for next round. If not, next evaluation will count the previous tests in.
            FileUtils.cleanUp(testPath,true);
            return result;
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
     * @throws Exception
     */
    public void evaluateTest(int type, HashMap<String,String> systemProperties) throws Exception {
        switch (type){
            case 0:
                log.info("Running coverage test on test files at {}, the target file are at {}",
                        testPath,targetPath);
                coverageTester.execute(systemProperties);
                log.info("The test result is: {}",coverageTester.getResultMap());
                break;
            case 1:
                evaluateTestMutation(systemProperties);
                break;
            case 100:
                log.info("Running coverage test on test files at {}, the target file are at {}",
                        testPath,targetPath);
                log.info("The result will be cloned as baseline.");

                coverageTester.execute(systemProperties);
                coverageThresholds = coverageTester.cloneResultMap();

                log.info("The baseline is: {}",coverageThresholds);
                break;
            default:
                break;
        }
    }
    private void evaluateTestMutation(HashMap<String,String> systemProperties){
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
                if(result.get("lines")<coverageThresholds.get("lines")){
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
