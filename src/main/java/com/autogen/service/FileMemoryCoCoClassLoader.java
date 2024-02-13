package com.autogen.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * A class loader that loads classes from in-memory data.
 */
public class FileMemoryCoCoClassLoader extends ClassLoader {
    /**
     * Store original class bytes, may be used when analyzing class.
     */
    private final Map<String, byte[]> oriDefinitions = new HashMap<>();

    /**
     * Store instrumented class bytes, used frequently when executing class or recording.
     */
    private final Map<String, byte[]> definitions = new HashMap<>();
    private final Set<String> targetNames = new HashSet<>();
    private final Set<String> testsNames = new HashSet<>();


    /**
     * Add a in-memory representation of a class.
     *
     * @param name
     *            name of the class
     * @param bytes
     *            class definition
     */
    public void addDefinition(String name, final byte[] bytes,boolean isTest) {
        if(isTest){
            testsNames.add(name);
        }else{
            targetNames.add(name);
        }
        definitions.put(name, bytes);
    }

    /**
     * Called usually when want to load the original class files as byte-arrays into memory.
     * @param rootClassPath
     *      The class path.
     * @param isTest
     *      Mark whether this is test-class path, only affect where the class file name stored in.
     */
    //TODO: 本质是读入一个文件夹下的所有.class文件，那么应该可以作为一个文件工具方法
    public void loadNormalClass(String rootClassPath, boolean isTest){
        File classPath = new File(rootClassPath);
        Deque<File> stack = new ArrayDeque<>();
        stack.push(classPath);

        if(classPath.exists() && classPath.isDirectory()){
            while(!stack.isEmpty()){
                classPath = stack.pop();
                int clazzPathLen = classPath.getAbsolutePath().length() + 1;
                File[] classFiles = classPath.listFiles(pathname -> {
                    //只加载class文件
                    return pathname.isDirectory() || pathname.getName().endsWith(".class");
                });
                if(classFiles==null)return;

                for(File classFile:classFiles){
                    if(classFile.isDirectory()){
                        stack.push(classFile);
                        continue;
                    }
                    String rawName = classFile.getAbsolutePath();
                    String className = rawName.substring(clazzPathLen, rawName.length() - 6);
                    className = className.replace(File.separatorChar, '.');

                    byte[] bytes = getClassByte(classFile);

                    addOriDefinition(className,bytes,isTest);
                }
            }
        }
    }

    /**
     * Only for load() in the original file.
     * @param className
     * @param bytes
     * @param isTest
     */
    private void addOriDefinition(String className, byte[] bytes, boolean isTest) {
        if(isTest){
            testsNames.add(className);
        }else{
            targetNames.add(className);
        }
        oriDefinitions.put(className,bytes);
    }

    private byte[] getClassByte(File classFile){
        if(classFile.exists()){
            byte[] bytes = new byte[0];
            try {
                FileInputStream fis = new FileInputStream(classFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                bytes = baos.toByteArray();
                fis.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("从本地读取class完成");
            return bytes;
        }else{
            return null;
        }
    }


    /**
     * Load class from memory, i.e. the byte array previously loaded by addDefinition.
     *
     * @param name
     *          The <a href="#binary-name">binary name</a> of the class
     *
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass(final String name)
            throws ClassNotFoundException {
        final byte[] bytes;
        bytes = definitions.get(name);

        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.loadClass(name);
    }

    public Set<String> getTargetNames() {
        return targetNames;
    }

    public Set<String> getTestsNames() {
        return testsNames;
    }

    public byte[] getOriginClassByte(String name){
        return oriDefinitions.get(name);
    }

    public byte[] getInstrumentedClassByte(String name){
        return definitions.get(name);
    }

}
