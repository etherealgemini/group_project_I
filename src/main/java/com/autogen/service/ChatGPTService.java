package com.autogen.service;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.BaseChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import lombok.extern.slf4j.Slf4j;
import retrofit2.HttpException;

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

    private static OpenAiClient openAiClient;
    private static ArrayList<String> history = new ArrayList<>();

    /**
     * A wrapper of .chat(string) method from chatGPTService.
     * @param prompt
     * @param responses
     *      Store previous responses.
     * @param respPointer
     *      Point to the last response of a conversation.(0,2,3) -> (0 for 1st; 1,2 for 2nd; 3 for 3rd)
     * @return
     */
    public synchronized String chat(String prompt,
                               ArrayList<String> responses,
                               ArrayList<Integer> respPointer) {
        log.info("Send prompt......");
        ArrayList<String> temp = chat(prompt);
        responses.addAll(temp);
        if(!respPointer.isEmpty())
            respPointer.add(respPointer.get(respPointer.size()-1) + responses.size()-1);
        else
            respPointer.add(responses.size()-1);
        return temp.get(0);
    }

    public void initializeChatService(String api){
        openAiClient = OpenAiClient.builder()
                .apiKey(Arrays.asList(api))
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .apiHost("https://api.chatanywhere.tech")
                .build();
//        history = new ArrayList<>();
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

        Message message = Message.builder().role(Message.Role.USER).content(history.toString()+msg).build();
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(Collections.singletonList(message))
                .model(BaseChatCompletion.Model.GPT_3_5_TURBO_1106.getName())
                .build();

        ChatCompletionResponse chatCompletionResponse;
        while(true) {
            try {
                chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
                break;
            } catch (HttpException e) {
                log.error(e.message());
            }
        }


        ArrayList<String> resp = new ArrayList<>();
        chatCompletionResponse.getChoices().forEach(e -> {
            System.out.println(e.getMessage().getContent());
            resp.add(e.getMessage().getContent());
            history.add(String.format("{role:user,history_content:%s}",msg));
            history.add(String.format("{role:ChatGPT,history_content:%s}",e.getMessage().getContent()));
        });
        return resp;
    }
    public boolean close(){
        openAiClient = null;
        history = null;
        return true;
    }
}
