import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class temp {
    public static void main(String[] args) {
        String a = "```java\n" +
                "public class AddNumbers {\n" +
                "    public static void main(String[] args) {\n" +
                "        int num1 = 1;\n" +
                "        int num2 = 1;\n" +
                "        int sum = num1 + num2;\n" +
                "        System.out.println(\"The sum of \" + num1 + \" and \" + num2 + \" is: \" + sum);\n" +
                "    }\n" +
                "}\n" +
                "```";
        int idx_1 = a.indexOf("```java")+"```java".length();
        int idx_2 = a.lastIndexOf("```");
        a = a.substring(idx_1,idx_2);
//        a = a.substring(7,a.length()-3);
        System.out.println(a);
        System.out.println("___________");
        System.out.println("write to java file");
        FileOutputStream fos = null;
        try {
            File file = new File("./temp_.java");
            fos = new FileOutputStream(file);
            System.out.println("文件创建成功！");
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            osw.write(a);
            osw.flush();
            osw.close();

        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
