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
        return "Below is the original unit test. Please refine or add new test cases to make it more complete, based on " +
                "the former PDF content. You need to give a complete test code. Here are the original unit test code.'''Java\n"+raw +"\n'''";
    }

    private static String pdfPrompt(String pdfContent){
        return "As a unit test specialist, I need you to help me with the refinement of some Java unit test, which is for an assignment." +
                "Below is the PDF content of an assignment, please understand the content and prepare for the upcoming test."
                + pdfContent;
    }

    private static String initialTestPrompt(String testFileContent){
        return "Below is the original unit test. Please refine or add new test cases to make it more complete, based on " +
                "the former PDF content. You need to give a complete test code. Here are the original unit test code.'''Java\n"+testFileContent +"\n'''";
    }
}
