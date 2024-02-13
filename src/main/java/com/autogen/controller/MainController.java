package com.autogen.controller;

import com.autogen.model.Code;
import com.autogen.model.FancyOutput;
import com.autogen.service.ChatGPTService;
import com.autogen.service.EvaluationService;
import com.autogen.utils.PromptType;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.autogen.utils.CompileUtils.compile;
import static com.autogen.utils.CommandLineUtils.run_cmd;
import static com.autogen.utils.IOUtils.*;
import static com.autogen.utils.PDFParser.parsePDFtoString;
import static com.autogen.utils.PromptUtils.prompting;

@Slf4j
public class MainController {

    private ResourceBundle autogen;
    private ChatGPTService chatGPTService;
    private HashMap<String,String> systemProperties;


    public void launch(){
        //0. 系统启动消息
        System.out.println(FancyOutput.SYSTEM_LAUNCH.getStr());
        System.out.println();

        //1. 读入资源文件
        systemProperties = new HashMap<>();
        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());

//        String humanTestInputPath = getPropertiesString(autogen,"originTestInputPath");
//        String programRootPath = getPropertiesString(autogen,"programRootPath");
//        String corePath = getPropertiesString(autogen,"corePath");
//        String libPath = getPropertiesString(autogen,"libPath");
//        String testPath = getPropertiesString(autogen,"testPath");
//        String targetPath = getPropertiesString(autogen,"targetPath");
//        String rootPath = getPropertiesString(autogen,"rootPath");
//        String evosuitePath = getPropertiesString(autogen,"evosuitePath");
//        String humanTestPath = getPropertiesString(autogen,"humanTestPath");
//        String evosuiteTestPath = getPropertiesString(autogen,"evosuiteTestPath");
        loadPathProperties();

        //2.1 编译目标程序文件至targetPath供后续评测时使用
        boolean comp = compile(systemProperties.get("programRootPath"),systemProperties.get("libPath"),
                systemProperties.get("targetPath"),systemProperties.get("programRootPath"));
        System.out.println();
        comp = compile(systemProperties.get("programRootPath"),systemProperties.get("libPath"),
                systemProperties.get("humanTestPath"),systemProperties.get("humanTestInputPath"));

        //2.2 pdf转string
        String PDFContent = parsePDFtoString(getPropertiesString(autogen,"pdfInputPath"));

        //2.3 后台运行Evosuite（耗时操作）
        String cmdOrigin = readFile("data\\core\\script_raw.bat");
        cmdOrigin = cmdOrigin.replace("EVOSUITE_PATH", systemProperties.get("evosuitePath"));
        cmdOrigin = cmdOrigin.replace("TARGET_PATH",systemProperties.get("targetPath"));
        cmdOrigin = cmdOrigin.replace("TEST_STORAGE_PATH",systemProperties.get("rootPath"));//-target TARGET_PATH -base_dir BASE_DIR_PATH
        writeFile("data\\core\\script.bat",cmdOrigin);

//        Thread evo = new Thread(()->{
//            run_cmd("data\\core\\script.bat");
//            compile(programRootPath,libPath,testPath,evosuiteTestPath);
//        });
//        evo.start();

        //2.4 后台进行人工测试评测，结果将作为Baseline（耗时操作）
        EvaluationService evaluationService =
                EvaluationService.getInstance(systemProperties.get("programRootPath"),systemProperties.get("targetPath"),
                        systemProperties.get("testPath"),systemProperties.get("rootPath"),systemProperties.get("libPath"));

        new Thread(() -> {
                    try {
                        evaluationService.evaluateTest(100,systemProperties);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();

        //3. prompt（第一次输入）
        chatGPTService = ChatGPTService.getInstance();
        chatGPTService.initializeChatService();

        ArrayList<Integer> respPointer = new ArrayList<>();
        ArrayList<String> responses = new ArrayList<>();

        //3.1 发送题目pdf
        String msg = prompting(PDFContent, PromptType.PDF_SUBMIT);
        String response = chat(msg,responses,respPointer);

        //3.2 发送测试
        //等待evosuite子线程完成。
//        while(evo.isAlive()){}
        File testFilePath = new File(systemProperties.get("humanTestInputPath"));
        File[] testFiles = testFilePath.listFiles();
        msg = prompting(readFile(testFiles[0].getAbsolutePath()),PromptType.REFINE_TEST);

//        msg = prompting(readFile(?),PromptType.INITIAL_TEST_SUBMIT);
        response = chat(msg,responses,respPointer);

        //4. 测试GPT结果
        Code evaluateResult = evaluationService.evaluateTestFromGPT(response,systemProperties);

        //5. 若满足输出条件，则10，否则7.
        boolean condition = evaluateResult.equals(Code.EVALUATION_PASS);
        while(!condition){
            //7. prompt（修正）
            String testContent = null;
            msg = prompting(testContent,PromptType.REFINE_TEST);
            response = chat(msg,responses,respPointer);

            //8. evaluation
            evaluateResult = evaluationService.evaluateTestFromGPT(response,systemProperties);

            //9. 重复7-8，直到满足输出条件
            condition = evaluateResult.equals(Code.EVALUATION_PASS);
        }

        //10. output and cleanup
        System.out.println("-----------------------------------------------");
        System.out.println();
    }

    private void loadPathProperties() {
        log.info("Load path configuration......");
        systemProperties.put("originTestInputPath",getPropertiesString(autogen,"originTestInputPath"));
        systemProperties.put("programRootPath",getPropertiesString(autogen,"programRootPath"));
        systemProperties.put("corePath",getPropertiesString(autogen,"corePath"));
        systemProperties.put("libPath",getPropertiesString(autogen,"libPath"));
        systemProperties.put("testPath",getPropertiesString(autogen,"testPath"));
        systemProperties.put("targetPath",getPropertiesString(autogen,"targetPath"));
        systemProperties.put("rootPath",getPropertiesString(autogen,"rootPath"));
        systemProperties.put("evosuitePath",getPropertiesString(autogen,"evosuitePath"));
        systemProperties.put("humanTestPath",getPropertiesString(autogen,"humanTestPath"));
        systemProperties.put("evosuiteTestPath",getPropertiesString(autogen,"evosuiteTestPath"));
    }

    private String chat(String prompt,
                      ArrayList<String> responses,
                      ArrayList<Integer> respPointer) {
        log.info("Send prompt......");
        ArrayList<String> temp = chatGPTService.chat(prompt);
        responses.addAll(temp);
        respPointer.add(responses.size()-1);
        return temp.get(0);
    }

}
