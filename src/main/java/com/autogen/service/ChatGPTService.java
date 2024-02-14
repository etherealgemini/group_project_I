package com.autogen.service;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ChatGPTService {
    private final static ChatGPTService CHAT_GPT_SERVICE = new ChatGPTService();
    private ChatGPTService(){}
    public static ChatGPTService getInstance(){
        return CHAT_GPT_SERVICE;
    }

    private OpenAiClient openAiClient;
    private ArrayList<String> history;

    /**
     * A wrapper of .chat(string) method from chatGPTService.
     * @param prompt
     * @param responses
     *      Store previous responses.
     * @param respPointer
     *      Point to the last response of a conversation.(0,2,3) -> (0 for 1st; 1,2 for 2nd; 3 for 3rd)
     * @return
     */
    public String chat(String prompt,
                               ArrayList<String> responses,
                               ArrayList<Integer> respPointer) {
        log.info("Send prompt......");
        ArrayList<String> temp = chat(prompt);
        responses.addAll(temp);
        respPointer.add(responses.size()-1);
        return temp.get(0);
    }

    public void initializeChatService(String api){
        openAiClient = OpenAiClient.builder()
                .apiKey(Arrays.asList(api))
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                //自己做了代理就传代理地址，没有可不不传
                .apiHost("https://api.chatanywhere.tech")
                .build();
        history = new ArrayList<>();
    }

    private ArrayList<String> chat(String msg){
        if(openAiClient==null){
            try {
                throw new RuntimeException("请先初始化ChatGPT服务");
            }catch (RuntimeException e){
                System.out.println(e.getMessage());
                return null;
            }
        }
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
