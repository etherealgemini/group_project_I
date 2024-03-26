package com.autogen.service;

import com.autogen.exception.MethodNotImplementException;
import com.autogen.model.Code;
import com.autogen.utils.FileUtils;
import com.autogen.utils.Formatter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static com.autogen.utils.CommandLineUtils.run_cmd;
import static com.autogen.utils.FileUtils.getClassesFiles;
import static com.autogen.utils.FileUtils.getClassesNames;
import static com.autogen.utils.IOUtils.*;
import static com.autogen.utils.MiscUtils.*;

@Slf4j
public class EvaluationService {
    private static Map<String,Double> coverageThresholds;
    private static final Map<String,Double> mutationResults = new HashMap<>();
    private static Map<String,Double> mutationThresholds;

    //    /**
//     * Compiled program path, used by system.
//     */
//    private static String targetPath;
//    /**
//     * Compiled tests path, used by system.
//     */
//    private static String testPath;
//    private static String rootPath;
//    private static String libPath;
//    private static String programRootPath;
    private final static EvaluationService EVALUATION_SERVICE = new EvaluationService();
    private static HashMap<String,String> systemProperties;


    // ******************
    // Mutation test indicators
    private static final String LC = "Line Coverage";
    private static final String MU_GEN = "Mutations generated";
    private static final String MU_KILL = "Mutations killed";
    private static final String MU_COVER = "Mutations coverage rate";
    private static final String MU_KILL_RATE = "Mutations killed rate";
    private static final String TOT_TEST = "total tests";
    private static final String TEST_STRENGTH = "Test strength";
    private static final double TEST_STRENGTH_DELTA = 0.2;
    // ******************


    private EvaluationService(){

    }
    public static EvaluationService getInstance(){
        return EVALUATION_SERVICE;
    }
    public static EvaluationService getInstance(HashMap<String,String> systemProperties){
        EvaluationService.systemProperties = systemProperties;
        return EVALUATION_SERVICE;
    }

    /**
     * 可以直接将gpt的回复字符串放进来。
     * 方法已加锁，防止第一次用户测试与后续测试同步执行导致系统异常。
     * @param raw
     * @return
     */
    public synchronized Code evaluateTestFromGPT(String raw){
        String code = Formatter.codeBlockFormatter(raw,"java");

        // Better not to reconstruct this method.
        Code cpResult = writeTestFileToJavaFile(code,systemProperties.get("rootPath"),
                systemProperties.get("programRootPath"),systemProperties.get("testPath"),systemProperties.get("libPath"));
        System.out.println("Io and compile result: "+cpResult);

        try {
            evaluateTest(202);
        } catch (Exception e){
            log.error(e.getMessage());
            return Code.EVALUATION_ERROR;
        }
        try {
            Code result = analyseResult(200);

            //clean up to prepare for next round. If not, next evaluation will count the previous tests in.
            FileUtils.cleanUp(systemProperties.get("testPath"),true);
            return result;
        } catch (Exception e){
            log.error(e.getMessage());
            return Code.EVALUATION_ANALYZE_ERROR;
        }
    }

    /**
     * 获取变异测试的结果。
     * @return
     */
    public String getMutationResults(){
        if(mutationResults==null || mutationResults.isEmpty()){
            log.warn("Not perform mutation test yet!");
            return null;
        }
        return mutationResults.toString();
    }

    private final CoverageTester coverageTester = new CoverageTester(System.out,new HashMap<>());

    /**
     *
     * @param type xyz
     *      <br>
     *      1yz: coverage test.<br>
     *      2yz: mutation test (Not implemented).<br>
     *      <br>
     *      y=0: baseline.<br>
     *      y=1: evosuite evaluation.<br>
     *      y=2: GPT evaluation.<br>
     * @throws Exception
     */
    public void evaluateTest(int type) throws Exception {
        switch (type){
            case 100:
                //coverage test baseline
                log.info("Running coverage test on test files at {}, the target file are at {}",
                        systemProperties.get("humanTestPath"),systemProperties.get("targetPath"));
                log.info("The result will be cloned as baseline.");

                coverageTester.execute(systemProperties, systemProperties.get("testPath"));
                coverageThresholds = coverageTester.cloneResultMap();

                log.info("The baseline is: {}",coverageThresholds);
                break;
            case 101:
                //coverage test evosuite
                log.info("Running coverage test on test files at {}, the target file are at {}",
                        systemProperties.get("testPath"),systemProperties.get("targetPath"));
                coverageTester.execute(systemProperties, systemProperties.get("testPath"));
                log.info("The test result is: {}",coverageTester.getResultMap());
                break;
            case 102:
                //coverage test gpt
                log.info("Running coverage test on test files at {}, the target file are at {}",
                        systemProperties.get("GPTTestPath"),systemProperties.get("targetPath"));
                coverageTester.execute(systemProperties, systemProperties.get("GPTTestPath"));
                log.info("The test result is: {}",coverageTester.getResultMap());
                break;
            case 200:
                //coverage test baseline
                log.info("Running mutation test on test files at {}, the target file are at {}",
                        systemProperties.get("humanTestPath"),systemProperties.get("targetPath"));
                log.info("The result will be cloned as baseline.");

                evaluateTestMutation(systemProperties, systemProperties.get("testPath"));
                mutationThresholds = cloneHashMap(mutationResults);

                log.info("The baseline is: {}",mutationThresholds);
                break;
//            case 201:
//                //coverage test evosuite
//                log.info("Running mutation test on test files at {}, the target file are at {}",
//                        systemProperties.get("testPath"),systemProperties.get("targetPath"));
//
//                evaluateTestMutation(systemProperties, systemProperties.get("testPath"));
//                log.info("The test result is: {}",mutationResults);
//                break;
            case 202:
                //coverage test gpt
                log.info("Running mutation test on test files at {}, the target file are at {}",
                        systemProperties.get("testPath"),systemProperties.get("targetPath"));
                evaluateTestMutation(systemProperties, systemProperties.get("testPath"));
                log.info("The test result is: {}",mutationResults);
                break;
            default:
                throw new MethodNotImplementException();
        }
    }

    /**
     * Tested
     * @param systemProperties
     * @param testPath
     */
    private void evaluateTestMutation(HashMap<String,String> systemProperties,String testPath) throws IOException {

        String cmdOrigin = readFile("data\\core\\PIT_script_raw.bat");
        cmdOrigin = cmdOrigin.replace("TEST_PATH", testPath);
        cmdOrigin = cmdOrigin.replace("TARGET_PATH", systemProperties.get("targetPath"));

        StringBuilder libs = new StringBuilder();
        libs.append(systemProperties.get("PITLibPath")).append("\\*;");
        libs.append(systemProperties.get("Junit5LibPath")).append("\\*;");
//        libs.append(systemProperties.get("Junit4LibPath")).append("\\*;");
        libs.append(systemProperties.get("EvoLibPath")).append("\\*;");

        libs.deleteCharAt(libs.length()-1);

        cmdOrigin = cmdOrigin.replace("PIT_LIB", libs);
        cmdOrigin = cmdOrigin.replace("REPORT_DIR", systemProperties.get("PITReportPath"));//-target TARGET_PATH -base_dir BASE_DIR_PATH

        String[] tests_ = getClassesNames(testPath);
        String[] targets_ = getClassesNames(systemProperties.get("targetPath"));

        String tests = arrayToString(tests_,",");
        String targets = arrayToString(targets_,",");

        cmdOrigin = cmdOrigin.replace("TESTS", tests);
        cmdOrigin = cmdOrigin.replace("TARGETS", targets);//-target TARGET_PATH -base_dir BASE_DIR_PATH

        cmdOrigin = cmdOrigin.replace("SRC_DIR", systemProperties.get("rootPath"));

        writeFile("data\\core\\PIT_script.bat", cmdOrigin);

        String tempReportPath = systemProperties.get("rootPath") + "\\temp\\PITResult";

        String tempStdReportPath = tempReportPath + "\\out";
        String tempErrReportPath = tempReportPath + "\\err";

        File tempReport = new File(tempReportPath);
        File tempStdReport = new File(tempStdReportPath);
        File tempErrReport = new File(tempErrReportPath);

        tempReport.mkdirs();

        tempStdReport.createNewFile();
        tempErrReport.createNewFile();

        run_cmd("data\\core\\PIT_script.bat",tempStdReportPath,tempErrReportPath);

//        TESTS
//        TARGETS

        BufferedReader reader = new BufferedReader(new FileReader(tempStdReport));

        String[] result = new String[4];
        int rp = 0;
        try(reader){
            String line;
            boolean startStore = false;
            while((line = reader.readLine())!=null){
                if(startStore){
                    if(line.contains("Enhanced functionality available at")){
                        break;
                    }
                    result[rp++] = line;
                }
                if(!startStore && line.contains("Statistics")){
                    startStore = true;
                    reader.readLine(); // skip separation line ===============================
                }
            }
        }catch (IOException e){
            log.error("Error occurred when reading PITest report",e);
        }

//        tempErrReport.deleteOnExit();
//        tempStdReport.deleteOnExit();
        mutationResultAnalyse(mutationResults, result);
    }

    /**
     * Notice that this is hard-coded.
     *
     * @param map
     * @param result
     */
    private static void mutationResultAnalyse(Map<String,Double> map, String[] result){
/*
================================================================================
- Statistics
================================================================================
>> Line Coverage (for mutated classes only): 109/125 (87%)
>> Generated 83 mutations Killed 73 (88%)
>> Mutations with no coverage 9. Test strength 99%
>> Ran 78 tests (0.94 tests per mutation)
*/

        map.put(LC,Double.parseDouble(findNum(result[0]).get(2))/100d);

        ArrayList<String> temp = findNum(result[1]);
        map.put(MU_GEN, Double.parseDouble(temp.get(0)));
        map.put(MU_KILL,Double.parseDouble(temp.get(1)));
        map.put(MU_COVER,1 - Double.parseDouble(findNum(result[2]).get(0))/Double.parseDouble(temp.get(0)));
        map.put(MU_KILL_RATE, Double.parseDouble(temp.get(1))/Double.parseDouble(temp.get(0)));

        temp = findNum(result[2]);
        map.put(TEST_STRENGTH,Double.parseDouble(temp.get(1))/100d);
    }

    public Code analyseResult(int type) {
        switch (type){
            case 100:{
                Map<String,Double> result = coverageTester.getResultMap();
                if(result.isEmpty()){
                    return Code.EVALUATION_ANALYZE_ERROR;
                }
                if(result.get("lines")<coverageThresholds.get("lines")){
                    return Code.WORSE_COVERAGE_LINE;
                }
                return Code.EVALUATION_PASS;
            }
            case 200:{
                if(mutationThresholds.get(TEST_STRENGTH) - mutationResults.get(TEST_STRENGTH)
                        <= Double.parseDouble(systemProperties.get("MutationTestThreshold"))){
                    return Code.EVALUATION_PASS;
                }
                else{
                    return Code.WORSE_MUTATION_TEST_STRENGTH;
                }
            }
            default:
                break;
        }
        return Code.EVALUATION_ANALYZE_ERROR;
    }
}
