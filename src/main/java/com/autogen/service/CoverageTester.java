
package com.autogen.service;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import com.autogen.utils.FileUtils;
import com.autogen.utils.ExtClasspathLoader;

import lombok.extern.slf4j.Slf4j;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.autogen.utils.MiscUtils.cloneHashMap;


/**
 * A single target class will be instrumented and executed. Finally the coverage information will be
 * dumped. To analyze the coverage of test, you need to load both the target and test class,
 * then execute the test class and analyze the target coverage.
 */
@Slf4j
public final class CoverageTester {

//    private static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);
    private final PrintStream out;
    private final HashMap<String,Double> resultMap;
    private boolean logging = true;

    /**
     * Creates a new example instance printing to the given stream.
     *
     * @param out
     *            stream for outputs
     * @param resultMap
     *            execute result dictionary, contains:
     *            "instructions","branches","lines","methods","complexity"
     *
     */
    public CoverageTester(final PrintStream out,final HashMap<String,Double> resultMap) {
        this.out = out;
        this.resultMap = resultMap;
    }
    public CoverageTester(final PrintStream out,final HashMap<String,Double> resultMap, boolean logging) {
        this(out, resultMap);
        this.logging = logging;
    }



    public void execute(HashMap<String,String> systemProperties,String testPath) throws Exception {
//        String testPath = systemProperties.get("testPath");
        String targetPath = systemProperties.get("targetPath");
        String rootPath = systemProperties.get("rootPath");

        String tempInstrTargetPath = rootPath + "\\temp\\targets\\instr";
        String tempInstrTestPath = rootPath + "\\temp\\tests\\instr";

//        FileMemoryCoCoClassLoader CoCoClassLoader = new FileMemoryCoCoClassLoader();
//        CoCoClassLoader.loadNormalClass(testPath,true);
//        CoCoClassLoader.loadNormalClass(targetPath,false);

        HashMap<String,byte[]> testHashmap = FileUtils.getFileByte(testPath,".class");
        HashMap<String,byte[]> targetHashmap = FileUtils.getFileByte(targetPath,".class");

        // For instrumentation and runtime we need a IRuntime instance
        // to collect execution data:

        final IRuntime runtime = new LoggerRuntime();

        // The Instrumenter creates a modified version of our test target class
        // that contains additional probes for execution data recording:
        final Instrumenter instr = new Instrumenter(runtime);

        // Now we're ready to run our instrumented class and need to startup the
        // runtime first:
        final RuntimeData data = new RuntimeData();
        runtime.startup(data);

        // In this tutorial we use a special class loader to directly load the
        // instrumented class definition from a byte[] instances.

//        TODO: no need to load in the origin file into classloader, read the bytes and instrument it, then load into classloader.

//        for(String className:CoCoClassLoader.getTargetNames()){
//            CoCoClassLoader.addDefinition(className,
//                    instr.instrument(CoCoClassLoader.getOriginClassByte(className),className),false
//            );
//        }
        File tempTargetFile = new File(tempInstrTargetPath);
        File tempTestFile = new File(tempInstrTestPath);
        if (!tempTargetFile.exists()){
            boolean err = tempTargetFile.mkdirs();
        }
        if (!tempTestFile.exists()){
            boolean err = tempTestFile.mkdirs();
        }

        for(String className:targetHashmap.keySet()){
            byte[] instrTemp = instr.instrument(targetHashmap.get(className),className);
            FileUtils.writeByte(instrTemp,tempTargetFile.getAbsolutePath()+"/"+className+".class");
        }
        for(String className:testHashmap.keySet()){
            byte[] instrTemp = instr.instrument(testHashmap.get(className),className);
            FileUtils.writeByte(instrTemp,tempTestFile.getAbsolutePath()+"/"+className+".class");
        }

//        for(String className:CoCoClassLoader.getTargetNames()){
//            CoCoClassLoader.addDefinition(className,
//                    instr.instrument(FileUtils.getByte(new File(className)),className),false
//            );
//        }
//
//        for(String className: CoCoClassLoader.getTestsNames()){
//            CoCoClassLoader.addDefinition(className,
//                    instr.instrument(CoCoClassLoader.getOriginClassByte(className),className),true
//            );
//        }

        ArrayList<URL> urls = new ArrayList<>();
        urls = FileUtils.loadJarFiles(urls,new File(systemProperties.get("libPath")));
        urls.add(new File(systemProperties.get("libPath")+"/").toURI().toURL());
        urls.add(new File(tempTestFile.getAbsolutePath()+"/").toURI().toURL());
        urls.add(new File(tempTargetFile.getAbsolutePath()+"/").toURI().toURL());
//        urls.add(new File(new File(testPath).getAbsolutePath()+"/").toURI().toURL());
//        urls.add(new File(new File(targetPath).getAbsolutePath()+"/").toURI().toURL());

        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]));

        // Here we execute our test target class by reflection, which should be instrumented:
        for(String className:targetHashmap.keySet()){
            Class<?> test = Class.forName(className,false,classLoader);
        }
        for(String className:testHashmap.keySet()){
            Class<?> test = Class.forName(className,false,classLoader);
            if(className.contains("scaffolding")){
                continue;
            }
            Object testO = test.newInstance();
            Method[] testMethods = test.getDeclaredMethods();
            //尝试排序，但不保证能按照原定义顺序。
            Arrays.sort(testMethods, Comparator.comparing(Method::getName));
            for(Method mo:testMethods){
                if(Arrays.stream(mo.getDeclaredAnnotations()).anyMatch(annotation -> annotation.toString().contains("Test"))){
                    try {
                        mo.invoke(testO);
                    } catch (Exception e){
                        log.warn("Test case "+mo.getName()+" failed.",e);
                    }
                }
            }
        }

        /*
        for(String className: CoCoClassLoader.getTestsNames()){
            Class<?> test = CoCoClassLoader.loadClass(className);
            Object testO = test.newInstance();
            Method[] testMethods = test.getDeclaredMethods();
            //会尝试排序，但不保证能按照原定义顺序。
            Arrays.sort(testMethods, Comparator.comparing(Method::getName));
            for(Method mo:testMethods){
                if(Arrays.stream(mo.getDeclaredAnnotations()).anyMatch(annotation -> annotation.toString().contains("Test"))){
                    mo.invoke(testO);
                }
            }
        }
        */

        // At the end of test execution we collect execution data and shutdown
        // the runtime:
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        // Together with the original class definition we can calculate coverage
        // information:
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

//        for(String className:CoCoClassLoader.getTargetNames()){
//
//        }
        for(String className:targetHashmap.keySet()){
            analyzer.analyzeClass(targetHashmap.get(className),className);
        }

        // Let's dump some metrics and line coverage information:
        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            out.printf("Coverage of class %s%n", cc.getName());

            printCounter("instructions", cc.getInstructionCounter(),resultMap);
            printCounter("branches", cc.getBranchCounter(),resultMap);
            printCounter("lines", cc.getLineCounter(),resultMap);
            printCounter("methods", cc.getMethodCounter(),resultMap);
            printCounter("complexity", cc.getComplexityCounter(),resultMap);

//            for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
//                out.printf("Line %s: %s%n", Integer.valueOf(i),
//                        getColor(cc.getLine(i).getStatus()));
//            }
        }

        if(logging){
            out.println("--------------");
            out.println("Coverage Result:");
            for(String type:resultMap.keySet()){
                out.printf("%s: %.3f%n",type,1-resultMap.get(type));
            }
        }

        FileUtils.cleanUp(tempInstrTestPath,false);
        FileUtils.cleanUp(tempInstrTargetPath,false);
//        System.out.println(resultMap);
    }

//    private InputStream getTargetClass(File name) throws IOException {
//        return Files.newInputStream(name.toPath());
//    }
//    private InputStream getTargetClass(final String name) {
//        final String resource = '/' + name.replace('.', '/') + ".class";
//        return getClass().getResourceAsStream(resource);
//    }

    private void printCounter(final String unit, final ICounter counter, HashMap<String,Double> resultMap) {
        final Integer missed = Integer.valueOf(counter.getMissedCount());
        final Integer total = Integer.valueOf(counter.getTotalCount());
        if (logging) out.printf("%.2f %s missed%n", (float)missed/total,unit);
        if(total.equals(0)){
            return;
        }
        if(resultMap.containsKey(unit)){
            resultMap.put(unit,resultMap.get(unit)+missed.doubleValue()/total.doubleValue());
        }else {
            resultMap.put(unit, missed.doubleValue()/total.doubleValue());
        }
    }

    private String getColor(final int status) {
        switch (status) {
            case ICounter.NOT_COVERED:
                return "red";
            case ICounter.PARTLY_COVERED:
                return "yellow";
            case ICounter.FULLY_COVERED:
                return "green";
        }
        return "";
    }

    /**
     * Creates a new example instance printing to the given stream.
     * @return
     *      execute result dictionary, contains:
     *      "instructions","branches","lines","methods","complexity"
     *
     */
    public HashMap<String, Double> getResultMap() {
        if(resultMap.isEmpty()){
            log.warn("The result dictionary is empty, may not execute yet!");
        }
        return resultMap;
    }

    public Map<String, Double> cloneResultMap() {
        if(resultMap.isEmpty()){
            log.warn("The result dictionary is empty, may not execute yet!");
        }
        return cloneHashMap(resultMap);
    }



    /**
     * Entry point to run this examples as a Java application.
     *
     * @param args
     *            list of program arguments
     * @throws Exception
     *             in case of errors
     */
    public static void main(final String[] args) throws Exception {
//        new CoverageTester(System.out,new HashMap<>()).execute("data\\targets","data\\tests");
    }

}