package com.unfbx.autogen;

import com.autogen.service.ChatGPTService;
import org.junit.Test;

import static com.autogen.utils.IOUtils.readFile;

public class ChatTest {

    @Test
    public void chatTest1(){
        ChatGPTService service = ChatGPTService.getInstance();
        service.initializeChatService();
        System.out.println(service.chat("你好"));

    }
    @Test
    public void readingTest1(){

    }
}
