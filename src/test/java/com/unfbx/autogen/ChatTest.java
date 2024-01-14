package com.unfbx.autogen;

import com.autogen.service.ChatGPTService;
import org.junit.Test;

import java.util.ArrayList;

public class ChatTest {

    @Test
    public void chatTest1(){
        ChatGPTService service = ChatGPTService.getInstance();
        service.initializeChatService();
        System.out.println(service.chat("你好"));
    }
}
