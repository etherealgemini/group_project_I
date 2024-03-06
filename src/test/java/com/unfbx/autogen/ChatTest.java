package com.unfbx.autogen;

import com.autogen.service.ChatGPTService;
import org.junit.Test;

import java.util.ArrayList;

import static com.autogen.utils.IOUtils.readFile;

public class ChatTest {

    @Test
    public void chatTest1(){

        ChatGPTService service = ChatGPTService.getInstance();
        service.initializeChatService("sk-OdN7wImvIqpGaU8OcwbLsrH2IlGbYMzLeOBMsxYmy7qXp5Vz");
        System.out.println(service.chat("你好",new ArrayList<>(),new ArrayList<>()));
        System.out.println(service.chat("你好",new ArrayList<>(),new ArrayList<>()));
    }
    @Test
    public void readingTest1(){

    }
}
