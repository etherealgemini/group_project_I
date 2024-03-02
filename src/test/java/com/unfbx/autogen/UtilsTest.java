package com.unfbx.autogen;

import cn.hutool.core.lang.hash.Hash;
import com.autogen.utils.FileUtils;
import org.junit.Assert;
import org.junit.Test;


import java.io.*;
import java.util.HashMap;

import static com.autogen.utils.MiscUtils.cloneHashMap;


public class UtilsTest {

    @Test
    public void byteWriteTest() throws IOException {
        byte[] input = new byte[]{'a','b','c'};
        FileUtils.writeByte(input,"D:/tmp");

        File file = new File("D:/tmp");
        Assert.assertTrue(file.exists());

        BufferedReader reader = new BufferedReader(new FileReader(file));
        Assert.assertArrayEquals(input, reader.readLine().getBytes());
    }

    @Test
    public void strToDoubleTest(){

    }

    @Test
    public void cloneHashMapTest(){
        HashMap<String,Double> a = new HashMap<>();
        a.put("a",1.1);
        a.put("b",1.2);
        HashMap<String,Double> b = (HashMap<String, Double>) cloneHashMap(a);

        a.put("a",1.6);
        Assert.assertNotEquals(a.get("a"),b.get("a"));
    }


}
