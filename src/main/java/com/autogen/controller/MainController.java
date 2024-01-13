package com.autogen.controller;

import com.autogen.model.Code;
import com.autogen.model.FancyOutput;
import com.autogen.service.ChatGPTService;
import com.autogen.service.EvaluationService;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.autogen.utils.CommandLineUtils.run_cmd;
import static com.autogen.utils.PDFParser.parsePDFtoString;

public class MainController {

    private ResourceBundle autogen;

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
        ChatGPTService chatGPTService = ChatGPTService.getInstance();
        chatGPTService.initializeChatService();
        ArrayList<String> responses = chatGPTService.chat(); //FIXME:需要处理prompt

        //4. 测试GPT结果
        Code evaluateResult = evaluationService.evaluateTestFromGPT(responses.get(0));

        //5. 若满足输出条件，则10，否则7.
        boolean condition = evaluateResult.equals(Code.EVALUATION_PASS);
        while(!condition){
            //7. prompt（修正）
            responses = chatGPTService.chat();
            //8. evaluation
            evaluateResult = evaluationService.evaluateTestFromGPT(responses.get(0));
            //9. 重复7-8，直到满足输出条件
            condition = evaluateResult.equals(Code.EVALUATION_PASS);
        }

        //10. output
        System.out.println("-----------------------------------------------");
        System.out.println();
    }


}
