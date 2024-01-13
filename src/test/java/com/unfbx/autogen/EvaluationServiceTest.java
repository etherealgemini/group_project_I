package com.unfbx.autogen;

import com.autogen.service.EvaluationService;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class EvaluationServiceTest {

    @Test
    public void testEvosuite(){
        String path = "data\\core\\evosuite-1.2.0.jar";
        File e = new File(path);
        Assert.assertTrue(e.exists());
    }


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
        EvaluationService controller = EvaluationService.getInstance();
        System.out.println(controller.evaluateTestFromGPT(temp));
    }
}
