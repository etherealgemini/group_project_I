package com.unfbx.autogen;

import com.autogen.utils.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

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
}
