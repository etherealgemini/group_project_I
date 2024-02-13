package com.unfbx.autogen;

import com.autogen.service.CoverageTester;
import com.autogen.service.EvaluationService;
import com.autogen.utils.FileUtils;
import com.autogen.utils.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static com.autogen.utils.CommandLineUtils.run_cmd;
import static com.autogen.utils.CommandLineUtils.run_cmd_example;
import static com.autogen.utils.CompileUtils.compile;
import static com.autogen.utils.FileUtils.loadJarFiles;
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
    private static HashMap<String,String> systemProperties = new HashMap<>();
    private static EvaluationService evaluationService =
            EvaluationService.getInstance(programRootPath,targetPath,testPath,rootPath,libPath);


    @Before
    public void init() {
        loadPathProperties();
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

//        evaluationService.evaluateTest(100,systemProperties);

        System.out.println(evaluationService.evaluateTestFromGPT(temp,systemProperties));
    }

    @Test
    public void evoCtest2() throws Exception {
        System.out.println(readFile(new File(humanTestInputPath).listFiles()[0].getAbsolutePath()));
//        evaluationService.evaluateTest(0,targetPath,testPath);
    }

    @Test
    public void classLoaderTest() throws Exception {
        File file = new File(systemProperties.get("libPath")+"/");
        ArrayList<URL> urls = new ArrayList<>();
        urls = loadJarFiles(urls,file);
        URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
        System.out.println(Arrays.toString(classLoader.getURLs()));
    }

    @Test
    public void canYouFindTheTest() throws ClassNotFoundException, MalformedURLException {
        File file = new File(systemProperties.get("libPath")+"/");
        File tempTestFile = new File(systemProperties.get("testPath") + "/");
        ArrayList<URL> urls = new ArrayList<>();
        urls = loadJarFiles(urls,file);
        URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL(),tempTestFile.toURI().toURL()});
        System.out.println(Arrays.toString(classLoader.getURLs()));

        HashMap<String,byte[]> testHashmap = FileUtils.getFileByte(systemProperties.get("testPath"),".class");
//        File tempTestFile = new File(systemProperties.get("testPath"));
        for(String className:testHashmap.keySet()){
            Class<?> test = Class.forName(className,false,classLoader);
            Assert.assertNotNull(test);
        }
    }

    @Test
    public void executeTest() throws Exception {
        HashMap result = new HashMap();
        CoverageTester tester = new CoverageTester(System.out,result,true);
        tester.execute(systemProperties);
    }

    @Test
    public void compileTest2(){
        compile(systemProperties.get("rootPath"),systemProperties.get("libPath"),
                systemProperties.get("testPath"),systemProperties.get("evosuiteTestPath"));
    }

    @Test
    public void executeTest2() throws Exception{
        HashMap result = new HashMap();
        CoverageTester tester = new CoverageTester(System.out,result,true);
        tester.execute(systemProperties);
    }



    public static HashMap<String,String> loadPathProperties() {
        autogen = ResourceBundle.getBundle("autogen", Locale.getDefault());
        systemProperties.put("originTestInputPath",getPropertiesString(autogen,"originTestInputPath"));
        systemProperties.put("programRootPath",getPropertiesString(autogen,"programRootPath"));
        systemProperties.put("corePath",getPropertiesString(autogen,"corePath"));
        systemProperties.put("libPath",getPropertiesString(autogen,"libPath"));
        systemProperties.put("testPath",getPropertiesString(autogen,"testPath"));
        systemProperties.put("targetPath",getPropertiesString(autogen,"targetPath"));
        systemProperties.put("rootPath",getPropertiesString(autogen,"rootPath"));
        systemProperties.put("evosuitePath",getPropertiesString(autogen,"evosuitePath"));
        systemProperties.put("humanTestPath",getPropertiesString(autogen,"humanTestPath"));
        systemProperties.put("evosuiteTestPath",getPropertiesString(autogen,"evosuiteTestPath"));
        return systemProperties;
    }


}
