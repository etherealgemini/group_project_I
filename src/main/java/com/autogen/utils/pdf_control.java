package com.autogen.utils;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class pdf_control {

    static ArrayList<String> history = new ArrayList<>();

    public static void main(String[] args) {
        OpenAiClient openAiClient = OpenAiClient.builder()
                .apiKey(Arrays.asList("sk-OdN7wImvIqpGaU8OcwbLsrH2IlGbYMzLeOBMsxYmy7qXp5Vz"))
                .keyStrategy(new KeyRandomStrategy())
                .apiHost("https://api.chatanywhere.tech")
                .build();

        Scanner s = new Scanner(System.in);


        System.out.println("请输入PDF文件路径：");
        String pdfFilePath = s.nextLine();


        // 读取PDF文件并进行分析
        List<String> pdfParagraphs = readAndSplitPDF(pdfFilePath, openAiClient);
        String refinedContent = String.join("\n", pdfParagraphs);


        // 输出 ChatGPT 的回复
        Message message = Message.builder().role(Message.Role.USER).content(String.valueOf(history)).build();
        ChatCompletion chatCompletion = ChatCompletion.builder().messages(Collections.singletonList(message)).build();
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);

        // 输出 ChatGPT 的回复
        chatCompletionResponse.getChoices().forEach(e -> {
            System.out.println(e.getMessage().getContent());
            history.add(String.format("{role:ChatGPT,content:%s}", e.getMessage().getContent()));
        });
    }

    public static List<String> readAndSplitPDF(String filePath, OpenAiClient openAiClient) {
        List<String> paragraphs = new ArrayList<>();
        FileInputStream is = null;
        PDDocument document = null;

        try {
            is = new FileInputStream(filePath);
            PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(is));
            parser.parse();
            document = parser.getPDDocument();
            PDFTextStripper stripper = new PDFTextStripper();
            int numberOfPages = document.getNumberOfPages();


            for (int i = 0; i < numberOfPages; i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String pageText = stripper.getText(document);


                // 将每一页的文本按段落切割
                String[] pageParagraphs = pageText.split("\n");

                // 对每一段进行提炼
                for (String paragraph : pageParagraphs) {
                    if (!paragraph.trim().isEmpty()) {
                        String refinedParagraph = chatGPTRefinement(openAiClient, paragraph.trim());
                        paragraphs.add(refinedParagraph);
                    }
                }
            }
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

        return paragraphs;
    }

    public static String chatGPTRefinement(OpenAiClient openAiClient, String inputText) {
        // 使用 ChatGPT 进行文本提炼
        Message message = Message.builder().role(Message.Role.USER).content(inputText).build();
        ChatCompletion chatCompletion = ChatCompletion.builder().messages(Collections.singletonList(message)).build();
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);//回复

        // 返回提炼后的文本
        return chatCompletionResponse.getChoices().get(0).getMessage().getContent();
    }
}
