package com.autogen.utils;

public enum PromptType {
    PDF_SUBMIT(1),
    INITIAL_TEST_SUBMIT(2),
    REFINE_TEST(3),
    OBTAIN_ONE_TEST_FROM_EVO(4),
    COMBINE_TEST_AND_RESULT(5),
    REFINE_TEST_2(6);

    private final int code;
    PromptType(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
