package com.unfbx.autogen;

import com.autogen.service.EvaluationService;
import com.unfbx.chatgpt.entity.whisper.Transcriptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class EvaluationServiceTest {

    private static HashMap<String,String> systemProperties = new HashMap<>();
    @Before
    public void init() {
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

        EvaluationService service = EvaluationService.getInstance(systemProperties);
        System.out.println(service.evaluateTestFromGPT(temp));
    }

    @Test
    public void mutationResultAnalysisTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String,Double> map = new HashMap<>();
        String[] result = {">> Line Coverage (for mutated classes only): 109/125 (87%)\n",
                ">> Generated 83 mutations Killed 73 (88%)\n",
                ">> Mutations with no coverage 9. Test strength 99%\n",
                ">> Ran 78 tests (0.94 tests per mutation)\n"};
        Class<?> cl = EvaluationService.class;
        Method m = cl.getDeclaredMethod("mutationResultAnalyse",Map.class,String[].class);
        m.setAccessible(true);
        m.invoke(cl,map,result);

        System.out.println(map);
    }

    @Test
    public void mutationTest() throws Exception {
        EvaluationService service = EvaluationService.getInstance(systemProperties);
        service.evaluateTest(200);
        System.out.println(service.);
    }
}
