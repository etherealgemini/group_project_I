import com.autogen.service.ChatGPTService;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import static com.autogen.utils.PDFParser.parsePDFtoString;

public class test {
    static ArrayList<String> history = new ArrayList<>();
    private static OpenAiClient openAiClient;
    public static void main(String[] args) {
//         openAiClient = OpenAiClient.builder()
//                .apiKey(Arrays.asList("sk-OdN7wImvIqpGaU8OcwbLsrH2IlGbYMzLeOBMsxYmy7qXp5Vz"))
//                //自定义key的获取策略：默认KeyRandomStrategy
//                .keyStrategy(new KeyRandomStrategy())
//                //自己做了代理就传代理地址，没有可不不传
//                .apiHost("https://api.chatanywhere.tech")
//                .build();
        ChatGPTService service = ChatGPTService.getInstance();
        service.initializeChatService("sk-OdN7wImvIqpGaU8OcwbLsrH2IlGbYMzLeOBMsxYmy7qXp5Vz");
        //聊天模型：gpt-3.5
        Scanner s = new Scanner(System.in);
//        history.add("以下是历史记录，你可以作为参考但不要返回在回答里");
//        run_cmd("D:\\temp\\evo_script_raw.bat");
        while(true){
            StringBuilder next = new StringBuilder();
            System.out.println("Start input (enter \"end input\" to finish):");
            WHILE:
            while(true){
                String _next = s.nextLine();
                switch (_next){
                    case "end input":
                        break WHILE;
                    case "file upload":
                        System.out.println("input file path: ");
                        String path = s.nextLine().trim();
                        String file = parsePDFtoString(path);

                        next.append("以下是一个pdf文件的全文，请你理解并做好准备。");
                        next.append("\n\n");
                        next.append(file);
                        next.append("\n\n");
                        next.append("pdf文件内容结束，请你理解以上内容并做好准备。");
                        break WHILE;
                    default:
                        next.append(_next);
                        break;
                }
            }
            if(next.toString().equals("quit chat")){
                break;
            }
//            chattt(next);
            ArrayList<String> resp = new ArrayList<>();
            ArrayList<Integer> pt = new ArrayList<>();
            service.chat(String.valueOf(next),resp,pt);
        }
    }

    private static void chattt(StringBuilder next) {
        Message message = Message.builder().role(Message.Role.USER).content(String.valueOf(history)+ next).build();
        ChatCompletion chatCompletion = ChatCompletion.builder().messages(Collections.singletonList(message)).build();
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
        chatCompletionResponse.getChoices().forEach(e -> {
            System.out.println(e.getMessage().getContent());
            history.add(String.format("{role:user,history_content:%s}", next));
            history.add(String.format("{role:ChatGPT,content:%s}",e.getMessage().getContent()));
        });
    }


//    public static void run_cmd(String strcmd) {
//
//        Runtime rt = Runtime.getRuntime(); //Runtime.getRuntime()返回当前应用程序的Runtime对象
//        Process ps = null;  //Process可以控制该子进程的执行或获取该子进程的信息。
//        try {
//            ps = rt.exec(strcmd);   //该对象的exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。
//            ps.waitFor();  //等待子进程完成再往下执行。
//        } catch (IOException | InterruptedException e1) {
//            e1.printStackTrace();
//        }
//
//        int i = 1;  //接收执行完毕的返回值
//        if (ps != null) {
//            i = ps.exitValue();
//        }
//        if (i == 0) {
//            System.out.println("执行完成.");
//        } else {
//            System.out.println("执行失败.");
//        }
//
//        if (ps != null) {
//            ps.destroy();  //销毁子进程
//        }
//        ps = null;
//    }
}