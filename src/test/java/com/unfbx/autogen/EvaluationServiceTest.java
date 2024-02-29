package com.unfbx.autogen;

import com.autogen.service.EvaluationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class EvaluationServiceTest {

    private static ResourceBundle autogen;
    private static HashMap<String,String> systemProperties;
    @Before
    public void init() {
        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());
        GeneralTest.loadPathProperties(systemProperties);
        //1. 从资源文件读入各类路径
//        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());
//        humanTestInputPath = getPropertiesString(autogen, "originTestInputPath");
//        programRootPath = getPropertiesString(autogen, "programRootPath");
//        corePath = getPropertiesString(autogen, "corePath");
//        libPath = getPropertiesString(autogen, "libPath");
//        testPath = getPropertiesString(autogen, "testPath");
//        targetPath = getPropertiesString(autogen, "targetPath");
//        rootPath = getPropertiesString(autogen, "rootPath");
//        evoPath = getPropertiesString(autogen, "evosuitePath");
//        humanTestPath = getPropertiesString(autogen, "humanTestPath");
//        evosuiteTestPath = getPropertiesString(autogen,"evosuiteTestPath");
    }
    @Test
    public void testEvosuite(){
        String path = "data\\core\\evosuite-1.2.0.jar";
        File e = new File(path);
        Assert.assertTrue(e.exists());
    }


    @Test
    public void testEvaluation() {

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

        EvaluationService controller = EvaluationService.getInstance(systemProperties);
        System.out.println(controller.evaluateTestFromGPT(temp));
    }
}
