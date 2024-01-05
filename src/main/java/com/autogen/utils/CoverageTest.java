package com.autogen.utils;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;

public class CoverageTest {

    /**
     * clsss的类目录
     */
    private File classesDirectory;
    /**
     * 源码目录
     */
    private File sourceDirectory;
    /**
     * exec文件目录
     */
    private File executionDataFile;

    /**
     * 报告生成目录
     */
    private File reportDirectory;

    public CoverageTest(File classesDirectory, File sourceDirectory, File executionDataFile, File reportDirectory) {
        this.classesDirectory = classesDirectory;
        this.sourceDirectory = sourceDirectory;
        this.executionDataFile = executionDataFile;
        this.reportDirectory = reportDirectory;
    }

    public void test(String path) {
        try {
            String projectDirectory = "D:\\IdeaProjects\\base\\base-service\\application";
            //目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
            File classesDirectory = new File(projectDirectory, "target\\classes\\com");
            File sourceDirectory = new File(projectDirectory, "src\\main\\java");
            //覆盖率的exec文件地址
            File executionDataFile = new File("D:\\jacoco\\jacoco-demo.exec");
            //要保存报告的地址
            File reportDirectory = new File("D:\\jacoco", "coveragereport");
            //解析exec
            ExecFileLoader execFileLoader = loadExecutionData(executionDataFile);
            //对比exec和class类，生成覆盖数据
            IBundleCoverage bundleCoverage = analyzeStructure(execFileLoader);
            //生成报告
            createReport(bundleCoverage, execFileLoader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 第一步解析exec文件
     *
     * @throws IOException
     */
    private ExecFileLoader loadExecutionData(File executionDataFile) throws IOException {
        // 解析exec
        ExecFileLoader execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);
        return execFileLoader;
    }


    private IBundleCoverage analyzeStructure(ExecFileLoader execFileLoader) throws IOException {

        //增量覆盖
//		final CoverageBuilder coverageBuilder = new CoverageBuilder("{method:"add"}");

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

        analyzer.analyzeAll(this.classesDirectory);

        return coverageBuilder.getBundle("报告率");
    }

    private void createReport(final IBundleCoverage bundleCoverage, ExecFileLoader execFileLoader)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(this.reportDirectory));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(), execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(this.sourceDirectory, "utf-8", 4));

        visitor.visitEnd();

    }


}


