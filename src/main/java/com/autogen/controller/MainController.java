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

    public void launch(){
        //0. 系统启动消息
        System.out.println(FancyOutput.SYSTEM_LAUNCH.getStr());

        //1. 读入资源文件
        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());

        String humanTestInputPath = getPropertiesString(autogen,"originTestInputPath");
        String programRootPath = getPropertiesString(autogen,"programRootPath");
        String corePath = getPropertiesString(autogen,"corePath");
        String libPath = getPropertiesString(autogen,"libPath");
        String testPath = getPropertiesString(autogen,"testPath");
        String targetPath = getPropertiesString(autogen,"targetPath");
        String rootPath = getPropertiesString(autogen,"rootPath");
        String evosuitePath = getPropertiesString(autogen,"evosuitePath");
        String humanTestPath = getPropertiesString(autogen,"humanTestPath");
        String evosuiteTestPath = getPropertiesString(autogen,"evosuiteTestPath");

        //2.1 编译目标程序文件至targetPath供后续评测时使用
        boolean comp = compile(programRootPath,libPath,targetPath,programRootPath);
        System.out.println();
        comp = compile(programRootPath,libPath,humanTestPath,humanTestInputPath);

        //2.2 pdf转string
        String PDFContent = parsePDFtoString(getPropertiesString(autogen,"pdfInputPath"));

        //2.3 后台运行Evosuite（耗时操作）
        String cmdOrigin = readFile("data\\core\\script_raw.bat");
        cmdOrigin = cmdOrigin.replace("EVOSUITE_PATH", evosuitePath);
        cmdOrigin = cmdOrigin.replace("TARGET_PATH",targetPath);
        cmdOrigin = cmdOrigin.replace("TEST_STORAGE_PATH",rootPath);//-target TARGET_PATH -base_dir BASE_DIR_PATH
        writeFile("data\\core\\script.bat",cmdOrigin);

//        Thread evo = new Thread(()->{
//            run_cmd("data\\core\\script.bat");
//            compile(programRootPath,libPath,testPath,evosuiteTestPath);
//        });
//        evo.start();

        //2.4 后台进行人工测试评测，结果将作为Baseline（耗时操作）
        EvaluationService evaluationService =
                EvaluationService.getInstance(programRootPath,targetPath,testPath,rootPath,libPath);

        new Thread(() -> {
                    try {
                        evaluationService.evaluateTest(100,targetPath,humanTestPath);
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
        File testFilePath = new File(humanTestInputPath);
        File[] testFiles = testFilePath.listFiles();
        msg = prompting(readFile(testFiles[0].getAbsolutePath()),PromptType.REFINE_TEST);

//        msg = prompting(readFile(?),PromptType.INITIAL_TEST_SUBMIT);
        response = chat(msg,responses,respPointer);

        //4. 测试GPT结果
        Code evaluateResult = evaluationService.evaluateTestFromGPT(response);

        //5. 若满足输出条件，则10，否则7.
        boolean condition = evaluateResult.equals(Code.EVALUATION_PASS);
        while(!condition){
            //7. prompt（修正）
            String testContent = null;
            msg = prompting(testContent,PromptType.REFINE_TEST);
            response = chat(msg,responses,respPointer);

            //8. evaluation
            evaluateResult = evaluationService.evaluateTestFromGPT(response);

            //9. 重复7-8，直到满足输出条件
            condition = evaluateResult.equals(Code.EVALUATION_PASS);
        }

        //10. output and cleanup
        System.out.println("-----------------------------------------------");
        System.out.println();
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
