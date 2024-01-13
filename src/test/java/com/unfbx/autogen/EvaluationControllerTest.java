package com.unfbx.autogen;

import com.autogen.controller.EvaluationController;
import org.junit.Test;

public class EvaluationControllerTest {

    @Test
    public void testGetEvaluation() {
        String temp = "```java"+
                "import java.lang.reflect.InvocationTargetException;\r\n" +
                "import org.junit.jupiter.api.Test;\r\n" +
                "\r\n" +
                "public class TargetTester {\r\n" +
                "    public TargetTester() {\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    @Test\r\n" +
                "    public void test1() throws InvocationTargetException, IllegalAccessException, InstantiationException {\r\n" +
                "        TestTarget testTarget = new TestTarget();\r\n" +
                "        testTarget.isPrime(0);\r\n" +
                "        testTarget.add(1, 2);\r\n" +
                "    }\r\n" +
                "}\r\n" +
                "```";
        EvaluationController controller = new EvaluationController();
        System.out.println(controller.evaluateTestFromGPT(temp));
    }
}
