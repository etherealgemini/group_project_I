package com.autogen.service;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChatGPTService {
    private final static ChatGPTService CHAT_GPT_SERVICE = new ChatGPTService();
    private ChatGPTService(){}
    public static ChatGPTService getInstance(){
        return CHAT_GPT_SERVICE;
    }

    private OpenAiClient openAiClient;
    private ArrayList<String> history;

    public void initializeChatService(){
        openAiClient = OpenAiClient.builder()
                .apiKey(Arrays.asList("sk-OdN7wImvIqpGaU8OcwbLsrH2IlGbYMzLeOBMsxYmy7qXp5Vz"))
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                //自己做了代理就传代理地址，没有可不不传
                .apiHost("https://api.chatanywhere.tech")
                .build();
        history = new ArrayList<>();
    }

    public ArrayList<String> chat(String msg){
        Message message = Message.builder().role(Message.Role.USER).content(history +msg).build();
        ChatCompletion chatCompletion = ChatCompletion.builder().messages(Collections.singletonList(message)).build();
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
        ArrayList<String> resp = new ArrayList<>();
        chatCompletionResponse.getChoices().forEach(e -> {
            System.out.println(e.getMessage().getContent());
            resp.add(e.getMessage().getContent());
            history.add(String.format("{role:user,history_content:%s}",msg));
            history.add(String.format("{role:ChatGPT,content:%s}",e.getMessage().getContent()));
        });
        return resp;
    }
}
