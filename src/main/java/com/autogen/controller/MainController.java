package com.autogen.controller;

import com.autogen.model.Code;
import com.autogen.model.FancyOutput;
import com.autogen.service.ChatGPTService;
import com.autogen.service.EvaluationService;
import com.autogen.utils.PromptType;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.autogen.utils.CommandLineUtils.run_cmd;
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

        //2.1 pdf转string
        String PDFContent = parsePDFtoString(autogen.getString("pdfInputPath"));
        //2.2 运行Evosuite
        run_cmd("data\\core\\script.bat");
        //2.3 后台进行人工测试评测，结果将作为Baseline
        String humanTestInputPath = autogen.getString("originTestInputPath");
        String programCodePath = autogen.getString("codeInputPath");

        EvaluationService evaluationService =
                EvaluationService.getInstance(programCodePath,humanTestInputPath);

        new Thread(() -> {
                    try {
                        evaluationService.evaluateTest(100,programCodePath,humanTestInputPath);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();

        //3. prompt（第一次输入）
        chatGPTService = ChatGPTService.getInstance();
        chatGPTService.initializeChatService();

        String msg = prompting(PDFContent, PromptType.PDF_SUBMIT);

        ArrayList<Integer> respPointer = new ArrayList<>();
        ArrayList<String> responses = new ArrayList<>();

        String response = chat(msg,responses,respPointer);

        msg = prompting(initialTestContent,PromptType.INITIAL_TEST_SUBMIT);
        response = chat(msg,responses,respPointer);

        //4. 测试GPT结果
        Code evaluateResult = evaluationService.evaluateTestFromGPT(responses.get(0));

        //5. 若满足输出条件，则10，否则7.
        boolean condition = evaluateResult.equals(Code.EVALUATION_PASS);
        while(!condition){
            //7. prompt（修正）
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
