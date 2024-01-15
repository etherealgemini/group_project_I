package com.unfbx.autogen;

import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static com.autogen.utils.CompileUtils.compile;


public class CompilerTest {

    @Test
    public void CompilerTest1(){
        String root = "D:\\Coding\\Creative\\group_project_I\\data";
        compile(root,root+"\\lib",root+"\\tests",root+"\\TargetTester.java");
    }

    @Test
    public void CompilerTest2(){
        String root = "D:\\Coding\\Creative\\group_project_I\\data";
        compile("D:\\Coding\\Creative\\autogenTest\\rawCode",root+"\\lib",root+"\\targets","D:\\Coding\\Creative\\autogenTest\\rawCode");
    }

    @Test
    public void javaFileTest1(){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null);

        File javaFile = new File("data");


        Iterable<? extends JavaFileObject> files = null;
        files = fileManager.getJavaFileObjectsFromFiles(
                List.of(Objects.requireNonNull(javaFile.listFiles((dir, name) -> name.endsWith(".java"))))
        );
        files.forEach(System.out::println);
    }

}
