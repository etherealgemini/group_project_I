package com.autogen.controller;

import com.autogen.service.EvaluateService;
import com.autogen.service.IOService;
import com.autogen.utils.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EvaluationController {
    public static final Logger logger = LoggerFactory.getLogger(EvaluationController.class);

    private static final int COVERAGE_THRESHOLD = 0;
    private static final int MUTATION_THRESHOLD = 0;
    private static final String tempFilePath = "./temp";
    EvaluateService evaluateService = new EvaluateService();
    IOService ioService = new IOService();

    /**
     * 可以直接将gpt的回复放进来，请放入content
     * @param raw
     * @return
     */
    public int evaluateTestFromGPT(String raw){
        String code = Formatter.codeBlockFormatter(raw,"java");

        try {
            int errCode = ioService.writeTestFileToJavaFile(code,tempFilePath,false);
        } catch (IOException e){
            logger.error("Cannot write test file string to java file!");
            return -1;
        }

        try {
            evaluateService.evaluateTest(0, tempFilePath);
            String[] result = evaluateService.analyseResult(tempFilePath);
        } catch (Exception e){
        //TODO: handle exception
            return -1;
        }



        /*TODO: if ? threshold then transfer to ...
         * call prompt engine
         * [receive user instruction]
         * output
         */
        return 0;
    }


}
