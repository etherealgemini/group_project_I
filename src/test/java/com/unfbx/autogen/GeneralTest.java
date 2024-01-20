package com.unfbx.autogen;

import com.autogen.service.EvaluationService;
import com.autogen.utils.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.autogen.utils.CommandLineUtils.run_cmd;
import static com.autogen.utils.CommandLineUtils.run_cmd_example;
import static com.autogen.utils.CompileUtils.compile;
import static com.autogen.utils.IOUtils.*;
import static com.autogen.utils.IOUtils.getPropertiesString;
import static com.autogen.utils.PDFParser.parsePDFtoString;

public class GeneralTest {
    private static ResourceBundle autogen;
    private static String humanTestInputPath;
    private static String programRootPath;
    private static String corePath;
    private static String libPath;
    private static String testPath;
    private static String targetPath;
    private static String rootPath;
    private static String evoPath;
    private static String humanTestPath;
    private static String evosuiteTestPath;
    private static EvaluationService evaluationService =
            EvaluationService.getInstance(programRootPath,targetPath,testPath,rootPath,libPath);


    @Before
    public void init() {
        //1. 从资源文件读入各类路径
        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());
        humanTestInputPath = getPropertiesString(autogen, "originTestInputPath");
        programRootPath = getPropertiesString(autogen, "programRootPath");
        corePath = getPropertiesString(autogen, "corePath");
        libPath = getPropertiesString(autogen, "libPath");
        testPath = getPropertiesString(autogen, "testPath");
        targetPath = getPropertiesString(autogen, "targetPath");
        rootPath = getPropertiesString(autogen, "rootPath");
        evoPath = getPropertiesString(autogen, "evosuitePath");
        humanTestPath = getPropertiesString(autogen, "humanTestPath");
        evosuiteTestPath = getPropertiesString(autogen,"evosuiteTestPath");
    }

    @Test
    public void PDFTest() {
        String pdf = parsePDFtoString(autogen.getString("pdfInputPath"));
        Assert.assertNotNull(pdf);
        System.out.println(pdf);
//        System.out.println(pdf);
    }

    @Test
    public void propertyTest1() {
        String v = IOUtils.getPropertiesString(autogen, "corePath");
        System.out.println(v);
    }

    @Test
    public void cmdTest1() {
        run_cmd_example();
    }

    @Test
    public void cmdTest2() {
        //1. 读入资源文件


        String cmdOrigin = readFile("data\\core\\script_raw.bat");
        cmdOrigin = cmdOrigin.replace("EVOSUITE_PATH", evoPath);
        cmdOrigin = cmdOrigin.replace("TARGET_PATH", programRootPath);
        cmdOrigin = cmdOrigin.replace("TEST_STORAGE_PATH", corePath);//-target TARGET_PATH -base_dir TEST_STORAGE_PATH
        writeFile("data\\core\\script.bat", cmdOrigin);
    }

    @Test
    public void cmdTest3() {
        run_cmd("data\\core\\script.bat");
    }

    @Test
    public void testEvaluation() throws Exception {

        String temp = "```java" +
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

        evaluationService.evaluateTest(100,targetPath,humanTestPath);

//        System.out.println(evaluationService.evaluateTestFromGPT(temp));
    }

    @Test
    public void evoCtest2() throws Exception {
        System.out.println(readFile(new File(humanTestInputPath).listFiles()[0].getAbsolutePath()));
//        evaluationService.evaluateTest(0,targetPath,testPath);
    }



}
