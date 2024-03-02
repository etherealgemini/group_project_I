package com.autogen.utils;

import java.io.*;

public class CommandLineUtils {
    public static void run_cmd_example(){
        String strcmd = "data\\core\\example.bat";
        run_cmd(strcmd);
    }
    public static void run_cmd(String strcmd) {

        Runtime rt = Runtime.getRuntime(); //Runtime.getRuntime()返回当前应用程序的Runtime对象
        Process ps = null;  //Process可以控制该子进程的执行或获取该子进程的信息。
        try {
            ps = rt.exec(strcmd);
            //该对象的exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。
//            BufferedReader inReader = new BufferedReader(
//                    new InputStreamReader(ps.getInputStream(),"GBK")
//            );
//            BufferedWriter outReader = new BufferedWriter(
//                    new OutputStreamWriter(ps.getOutputStream(),"GBK")
//            );
//            BufferedReader errReader = new BufferedReader(
//                    new InputStreamReader(ps.getErrorStream(),"GBK")
//            );
             //等待子进程完成再往下执行。
            new StreamClearThread(ps.getInputStream()).start();
            new StreamClearThread(ps.getErrorStream()).start();
//            String inLine;
//            String errLine;
//            while(true){
//                errLine = errReader.readLine();
//                inLine = inReader.readLine();
//                if(inLine!=null)System.out.println(inLine);
//                if(errLine!=null)System.out.println(errLine);
//                if(inLine==null&&errLine==null)break;
//            }
            ps.waitFor();
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

    public static void run_cmd(String strcmd, String stdFilePath, String errFilePath) {

        Runtime rt = Runtime.getRuntime(); //Runtime.getRuntime()返回当前应用程序的Runtime对象
        Process ps = null;  //Process可以控制该子进程的执行或获取该子进程的信息。
        try {
            ps = rt.exec(strcmd);
            //该对象的exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。
            //等待子进程完成再往下执行。
            new StreamClearThread_File(ps.getInputStream(),stdFilePath).start();
            new StreamClearThread_File(ps.getErrorStream(),errFilePath).start();

            ps.waitFor();
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


    public static class StreamClearThread extends Thread
    {
        private final BufferedReader reader;
        public StreamClearThread(InputStream stream) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(stream));
        }

        public void run()
        {
            try(reader) {
                String line;
                String lline = "";
                while (true) {
                    line = reader.readLine();
                    if (line == null) break;
                    if (!line.equals(lline)) {
                        System.out.println(line);
                    }
                    lline = line;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public static class StreamClearThread_File extends Thread
    {
        private final BufferedReader reader;
        private final BufferedWriter writer;
        public StreamClearThread_File(InputStream stream,String file) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(stream));
            this.writer = new BufferedWriter(new FileWriter(file));
        }

        public void run()
        {
            try(reader;writer) {
                String line;
                String lline = "";
                while (true) {
                    line = reader.readLine();
                    if (line == null) break;
                    if (!line.equals(lline)) {
                        System.out.println(line);
                        writer.write(line+"\n");
                    }
                    lline = line;
                }
                writer.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}



