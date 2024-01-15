package com.autogen.utils;

public class PromptUtils {
    private PromptUtils(){}

    public static String prompting(String raw, PromptType type){
        String out = "";
        switch (type){
            case PDF_SUBMIT:
                out = pdfPrompt(raw);
                break;
            case INITIAL_TEST_SUBMIT:
                out = initialTestPrompt(raw);
                break;
            case REFINE_TEST:
                out = refineTestPrompt(raw);
                break;
            default:
                out = raw;
        }
        return out;
    }

    private static String refineTestPrompt(String raw) {
        return "";
    }

    private static String pdfPrompt(String pdfContent){
        return "";
    }

    private static String initialTestPrompt(String testFileContent){
        return "";
    }
}
