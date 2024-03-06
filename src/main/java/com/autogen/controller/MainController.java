package com.autogen.controller;

import com.autogen.model.Code;
import com.autogen.model.FancyOutput;
import com.autogen.service.ChatGPTService;
import com.autogen.service.EvaluationService;
import com.autogen.utils.MiscUtils;
import com.autogen.utils.PromptType;
import com.autogen.utils.PromptUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

import static com.autogen.utils.CommandLineUtils.run_cmd;
import static com.autogen.utils.CompileUtils.compile;
import static com.autogen.utils.IOUtils.*;
import static com.autogen.utils.PDFParser.parsePDFtoString;
import static com.autogen.utils.PromptUtils.combineTestAndResult;
import static com.autogen.utils.PromptUtils.prompting;

/**
 * @author Zheng, K. S.
 * @version 1.0
 * <p>
 * 这个类控制整个系统的运行流程，并对外暴露系统的启动api。
 */
@Slf4j
public class MainController {

    private static ResourceBundle autogen;
    private static HashMap<String, String> systemProperties;
    private static EvaluationService evaluationService;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * 启动系统。
     */
    public void launch() {
        // 该方法不负责任何系统内部的实现，仅完成调用。具体实现委派给了具体类。
        //0. 系统启动消息
        System.out.println(FancyOutput.SYSTEM_LAUNCH.getStr());
        System.out.println("AutoGen is launched at: " + MiscUtils.getNow());

        ChatGPTService chatGPTService = ChatGPTService.getInstance(); // 不需要暴露系统配置的话，systemProperties就不必给他了
        System.out.println("Please enter your ChatGPT api key:");
        chatGPTService.initializeChatService(scanner.next());

        System.out.println("CharGPT service initialization success!");
        //1. 读入资源文件
        systemProperties = new HashMap<>();
        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());
        loadSystemProperties();

        //2.1 编译目标程序文件至targetPath供后续评测时使用
        //NOTE: 没有用systemProperties重构compile方法，因为有可能重构后看不懂输入。
        boolean err = compile(systemProperties.get("programRootPath"), systemProperties.get("libPath"),
                systemProperties.get("targetPath"), systemProperties.get("programRootPath"));
        CHECKERR(err);
        err = compile(systemProperties.get("programRootPath"), systemProperties.get("libPath"),
                systemProperties.get("humanTestPath"), systemProperties.get("originTestInputPath"));
        CHECKERR(err);

        //2.2 pdf转string
        String PDFContent = parsePDFtoString(getPropertiesString(autogen, "pdfInputPath"));

        //2.3 后台运行Evosuite，生成测试、编译，并进行评测（耗时操作）
//        Thread evo = new Thread(this::runEvosuite);

        //2.4 后台进行人工测试评测，结果将作为Baseline（耗时操作）
        evaluationService =
                EvaluationService.getInstance(systemProperties); //传入系统配置

        try {
            evaluationService.evaluateTest(201);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

        ArrayList<Integer> respPointer = new ArrayList<>();
        ArrayList<String> responses = new ArrayList<>();

        //3.1 发送题目pdf
        String msg = "";
        String response = "";

        msg = PromptUtils.prompting(PDFContent, PromptType.PDF_SUBMIT);
        chatGPTService.chat(msg, responses, respPointer);

        //3.2 发送测试
        //等待evosuite子线程完成。
//        try {
//            evo.join();
//        } catch (InterruptedException e) {
//            log.error("evosuite子线程被异常中断", e);
//            return;
//        }

//        while(evo.isAlive()){}
        //TODO: 这里有来自Evosuite的一系列的，针对不同class的测试文件，需要进行合适的prompt来指导GPT生成测试。
        File testFilePath = new File(systemProperties.get("evosuiteTestPath"));
        File[] testFiles = testFilePath.listFiles();

        HashMap<String,String> testContent = null;
        if (testFiles != null) {
            testContent = readFiles(testFiles);
        }

        msg = PromptUtils.prompting(String.valueOf(testContent), PromptType.OBTAIN_ONE_TEST_FROM_EVO);

        response = chatGPTService.chat(msg, responses, respPointer);

        //4. 测试GPT结果
        Code evaluateResult = evaluationService.evaluateTestFromGPT(response);
        //通过getMutationResults获取变异测试的结果，为HashMap的toString结果。
        String result = evaluationService.getMutationResults();

        //5. 若满足输出条件，则10，否则7.
        boolean condition = evaluateResult.equals(Code.EVALUATION_PASS);
        while (!condition) {
            //7. prompt（修正）进入循环，如果评测结果不满足输出条件
            testFilePath = new File(systemProperties.get("GPTTestPath"));
            testFiles = Objects.requireNonNull(testFilePath.listFiles());

            //TODO: 给gpt生成的test进行prompt来进行修正。
            testContent = readFiles(testFiles);

            msg = PromptUtils.combineTestAndResult(String.valueOf(testContent), result);

            response = chatGPTService.chat(msg, responses, respPointer);

            //8. evaluation
            evaluateResult = evaluationService.evaluateTestFromGPT(response);
            result = evaluationService.getMutationResults();

            //9. 重复7-8，直到满足输出条件
            condition = evaluateResult.equals(Code.EVALUATION_PASS);
        }

        //10. output and cleanup
        chatGPTService.close();
        System.out.printf("Finish generation. Please check the test files at %s\n",systemProperties.get("testPath"));
        System.out.println("-----------------------------------------------");
        System.out.println();
    }

    /**
     * 这个方法适用于检查某不可容忍的错误发生的情况。
     * 当err为真时，抛出**运行时异常**。
     * @param err
     *      Error flag.
     * @throws RuntimeException
     */
    private void CHECKERR(boolean err) throws RuntimeException {
        if (!err) {
            log.error("Error occurred.");
            throw new RuntimeException();
        }
    }

    private void runEvosuite() {
        String cmdOrigin = readFile("data\\core\\evo_script_raw.bat");
        cmdOrigin = cmdOrigin.replace("EVOSUITE_PATH", systemProperties.get("evosuitePath"));
        cmdOrigin = cmdOrigin.replace("TARGET_PATH", systemProperties.get("targetPath"));
        cmdOrigin = cmdOrigin.replace("TEST_STORAGE_PATH", systemProperties.get("rootPath"));//-target TARGET_PATH -base_dir BASE_DIR_PATH
        writeFile("data\\core\\evo_script.bat", cmdOrigin);

        run_cmd("data\\core\\evo_script.bat");
        compile(systemProperties.get("rootPath"), systemProperties.get("libPath"),
                systemProperties.get("testPath"), systemProperties.get("evosuiteTestPath"));

        try {
            evaluationService.evaluateTest(101);
        } catch (Exception e) {
            log.error("Evosuite评测异常", e);
        }
    }

    private void loadSystemProperties() {
        log.info("Load path configuration......");
        for (String key : autogen.keySet()) {
            systemProperties.put(key,getPropertiesString(autogen,key));
        }
//        systemProperties.put("originTestInputPath", getPropertiesString(autogen, "originTestInputPath"));
//        systemProperties.put("programRootPath", getPropertiesString(autogen, "programRootPath"));
//        systemProperties.put("corePath", getPropertiesString(autogen, "corePath"));
//        systemProperties.put("libPath", getPropertiesString(autogen, "libPath"));
//        systemProperties.put("testPath", getPropertiesString(autogen, "testPath"));
//        systemProperties.put("targetPath", getPropertiesString(autogen, "targetPath"));
//        systemProperties.put("rootPath", getPropertiesString(autogen, "rootPath"));
//        systemProperties.put("evosuitePath", getPropertiesString(autogen, "evosuitePath"));
//        systemProperties.put("humanTestPath", getPropertiesString(autogen, "humanTestPath"));
//        systemProperties.put("evosuiteTestPath", getPropertiesString(autogen, "evosuiteTestPath"));
//        systemProperties.put("GPTTestPath", getPropertiesString(autogen, "GPTTestPath"));
//        systemProperties.put("ChatGPTApi", getPropertiesString(autogen, "ChatGPTApi"));
    }

}

/*
TODO: 1. 设计prompt确保gpt总是会返回一个完整的测试文件
TODO: 2.
 */

/*
这是mutation test返回的原始结果，会基本按照这种方式解析。
>> Line Coverage (for mutated classes only): 109/125 (87%)
>> Generated 83 mutations Killed 73 (88%)
>> Mutations with no coverage 9. Test strength 99%
>> Ran 78 tests (0.94 tests per mutation)
 */
