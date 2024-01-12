
package com.autogen.utils;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import com.autogen.controller.EvaluationController;
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


/**
 * A single target class will be instrumented and executed. Finally the coverage information will be
 * dumped. To analyze the coverage of test, you need to load both the target and test class,
 * then execute the test class and analyze the target coverage.
 */
@Slf4j
public final class CoverageTester {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationController.class);
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


    /**
     * Run this example.
     *
     * @throws Exception
     *             in case of errors
     */
    public void execute(String targetPath,String testPath) throws Exception {


        FileMemoryCoCoClassLoader CoCoClassLoader = new FileMemoryCoCoClassLoader();
        CoCoClassLoader.load(testPath,true);
        CoCoClassLoader.load(targetPath,false);

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

        for(String className:CoCoClassLoader.getTargetNames()){
            CoCoClassLoader.addDefinition(className,
                    instr.instrument(CoCoClassLoader.getOriginClassByte(className),className),false
            );
        }

        for(String className: CoCoClassLoader.getTestsNames()){
            CoCoClassLoader.addDefinition(className,
                    instr.instrument(CoCoClassLoader.getOriginClassByte(className),className),true
            );
        }

        // Here we execute our test target class by reflection:

        for(String className: CoCoClassLoader.getTestsNames()){
            Class<?> test = CoCoClassLoader.loadClass(className);
            Object testO = test.newInstance();
            Method[] testMethods = test.getDeclaredMethods();
            for(Method mo:testMethods){
                if(Arrays.stream(mo.getDeclaredAnnotations()).anyMatch(annotation -> {
                    System.out.println(annotation);
                    return annotation.toString().contains("Test");
                })){
                    mo.invoke(testO);
                }
            }
        }

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

        for(String className:CoCoClassLoader.getTargetNames()){
            analyzer.analyzeClass(CoCoClassLoader.getOriginClassByte(className),className);
        }

        // Let's dump some metrics and line coverage information:
        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            out.printf("Coverage of class %s%n", cc.getName());

            printCounter("instructions", cc.getInstructionCounter(),resultMap);
            printCounter("branches", cc.getBranchCounter(),resultMap);
            printCounter("lines", cc.getLineCounter(),resultMap);
            printCounter("methods", cc.getMethodCounter(),resultMap);
            printCounter("complexity", cc.getComplexityCounter(),resultMap);

            for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
                out.printf("Line %s: %s%n", Integer.valueOf(i),
                        getColor(cc.getLine(i).getStatus()));
            }
        }
    }

    private InputStream getTargetClass(File name) throws IOException {
        return Files.newInputStream(name.toPath());
    }
    private InputStream getTargetClass(final String name) {
        final String resource = '/' + name.replace('.', '/') + ".class";
        return getClass().getResourceAsStream(resource);
    }

    private void printCounter(final String unit, final ICounter counter, HashMap<String,Double> resultMap) {
        final Integer missed = Integer.valueOf(counter.getMissedCount());
        final Integer total = Integer.valueOf(counter.getTotalCount());
        if (logging) out.printf("%.2f %s missed%n", (float)missed/total,unit);
        resultMap.put(unit, missed.doubleValue()/total.doubleValue());
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
            logger.warn("The result dictionary is empty, may not execute yet!");
        }
        return resultMap;
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
        new CoverageTester(System.out,new HashMap<>()).execute("data\\targets","data\\tests");
    }

}