import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class prompt {
    static ArrayList<String> history = new ArrayList<>();
    public static void main(String[] args) {
        OpenAiClient openAiClient = OpenAiClient.builder()
                .apiKey(Arrays.asList("sk-OdN7wImvIqpGaU8OcwbLsrH2IlGbYMzLeOBMsxYmy7qXp5Vz"))
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                //自己做了代理就传代理地址，没有可不不传
                .apiHost("https://api.chatanywhere.tech")
                .build();
        //聊天模型：gpt-3.5
        Scanner s = new Scanner(System.in);
        history.add("以下是历史记录，你可以作为参考但不要返回在回答里");
        run_cmd("D:\\temp\\script_raw.bat");

        // 新增：读取生成的 .test 文件内容并加入 history 列表
        String testFileContent = readTestFile("D:\\temp\\output.test");
        history.add(String.format("{role:system,history_content:%s}", testFileContent));
//处理原始的.test
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
                        String file = uploadFile(path);

                        next.append("以下是题目的要求");
                        next.append("\n\n");
                        next.append(file);
                        next.append("\n\n");
                        next.append("题目内容结束，请上传 .test 文件。");
                        break ;

                    case "test upload":
                        next.append("以下是evosuite生成的测试文件：");
                        next.append("\n\n");
                        next.append(testFileContent);
                        next.append("\n\n");
                        next.append("测试文件内容结束，请你根据题目的要求帮我补全测试文件。");
                        break WHILE;
                    default:
                        next.append(_next);
                        break;
                }
            }
            if(next.toString().equals("quit chat")){
                break;
            }

            Message message = Message.builder().role(Message.Role.USER).content(String.valueOf(history)+next).build();
            ChatCompletion chatCompletion = ChatCompletion.builder().messages(Collections.singletonList(message)).build();
            ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
            chatCompletionResponse.getChoices().forEach(e -> {
                System.out.println(e.getMessage().getContent());
                history.add(String.format("{role:user,history_content:%s}",next));
                history.add(String.format("{role:ChatGPT,content:%s}",e.getMessage().getContent()));
            });
        }



        //处理evolution的.test
        while (true){
            String evolutionTestFileContent = readEvolutionTestFile("D:\\temp\\evolution.java.test");
            history.add(String.format("{role:evolution,history_content:%s}", evolutionTestFileContent));

        }
    }
    public static String uploadFile(String filePath){
        String result = null;
        FileInputStream is = null;
        PDDocument document = null;
        try {
            is = new FileInputStream(filePath);
            PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(is));
            parser.parse();
            document = parser.getPDDocument();
            PDFTextStripper stripper = new PDFTextStripper();
            result = stripper.getText(document);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    public static void run_cmd(String strcmd) {
//
        Runtime rt = Runtime.getRuntime(); //Runtime.getRuntime()返回当前应用程序的Runtime对象
        Process ps = null;  //Process可以控制该子进程的执行或获取该子进程的信息。
        try {
            ps = rt.exec(strcmd);   //该对象的exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。
            ps.waitFor();  //等待子进程完成再往下执行。
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }

        int i = 1;  //接收执行完毕的返回值
        if (ps != null) {
            i = ps.exitValue();
        }
        if (i == 0) {
            System.out.println("执行完成.");
        } else {
            System.out.println("执行失败.");
        }

        if (ps != null) {
            ps.destroy();  //销毁子进程
        }
        ps = null;
    }

//读取evosuite生成的.test文件
    public static String readTestFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    //读取evolution 之后的.test文件
    public static String readEvolutionTestFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return content.toString();
    }


}