package com.autogen.controller;

import com.autogen.model.Code;
import com.autogen.service.EvaluateService;
import com.autogen.service.IOService;
import com.autogen.utils.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class EvaluationController {
    public static final Logger logger = LoggerFactory.getLogger(EvaluationController.class);

    private static final int COVERAGE_THRESHOLD = 0;
    private static final int MUTATION_THRESHOLD = 0;
    private static final String rootPath = "/data";
    EvaluateService evaluateService = new EvaluateService();
    IOService ioService = new IOService();

    /**
     * 可以直接将gpt的回复放进来，请放入content
     * @param raw
     * @return
     */
    public Code evaluateTestFromGPT(String raw){
        String code = Formatter.codeBlockFormatter(raw,"java");

        try {
            ioService.writeTestFileToJavaFile(code, rootPath,false);
        } catch (IOException e){
            logger.error("Cannot write test file string to java file!");
            return Code.EVALUATION_IO_WRITING_ERROR;
        }

        try {
            evaluateService.evaluateTest(0, rootPath);
        } catch (Exception e){
            logger.error(e.getMessage());
            return Code.EVALUATION_ERROR;
        }
        try {
            return evaluateService.analyseResult(0);
        } catch (Exception e){
            logger.error(e.getMessage());
            return Code.EVALUATION_ANALYZE_ERROR;
        }
    }


}
