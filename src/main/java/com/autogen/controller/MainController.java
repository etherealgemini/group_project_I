package com.autogen.controller;

import com.autogen.model.Code;
import com.autogen.model.FancyOutput;
import com.autogen.service.ChatGPTService;
import com.autogen.service.EvaluationService;
import com.autogen.utils.PromptType;

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

        //2.1 pdf转string
        String PDFContent = parsePDFtoString(getPropertiesString(autogen,"pdfInputPath"));

        //2.2 运行Evosuite
        String cmdOrigin = readFile("data\\core\\script_raw.bat");
        cmdOrigin = cmdOrigin.replace("TARGET_PATH",programRootPath);
        cmdOrigin = cmdOrigin.replace("BASE_DIR_PATH",corePath);//-target TARGET_PATH -base_dir BASE_DIR_PATH
        writeFile("data\\core\\script.bat",cmdOrigin);

        run_cmd("data\\core\\script.bat");

        //2.3 编译目标程序文件至targetPath供后续评测时使用
        compile(programRootPath,libPath,targetPath,programRootPath);

        //2.4 后台进行人工测试评测，结果将作为Baseline
        EvaluationService evaluationService =
                EvaluationService.getInstance(programRootPath,targetPath,testPath,rootPath,libPath);

        new Thread(() -> {
                    try {
                        evaluationService.evaluateTest(100,programRootPath,humanTestInputPath);
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

        //3.2 发送初始Evosuite生成测试
//        msg = prompting(readFile(?),PromptType.INITIAL_TEST_SUBMIT);
        response = chat(msg,responses,respPointer);

        //4. 测试GPT结果
        Code evaluateResult = evaluationService.evaluateTestFromGPT(responses.get(0));

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

        //10. output
        System.out.println("-----------------------------------------------");
        System.out.println();
    }

    private String chat(String prompt,
                      ArrayList<String> responses,
                      ArrayList<Integer> respPointer) {
        ArrayList<String> temp = chatGPTService.chat(prompt);
        responses.addAll(temp);
        respPointer.add(responses.size()-1);
        return temp.get(0);
    }

}
