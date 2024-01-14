package com.autogen.utils;

public enum PromptType {
    PDF_SUBMIT(1),
    INITIAL_TEST_SUBMIT(2),
    REFINE_TEST(3)
    ;

    private final int code;
    PromptType(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
