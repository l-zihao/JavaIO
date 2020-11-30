package socket;

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        final String QUIT = "quit";
        final String DEFAULT_SERVER_HOST = "127.0.0.1";
        final int DEFAULT_SERVER_PORT = 8828;

        Socket socket = null;
        BufferedWriter writer = null;

        try {
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            BufferedReader consoleReader = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            while (true){
                //等待用户输入信息
                String input = consoleReader.readLine();

                //发送信息给服务器
                writer.write(input + "\n");
                writer.flush();

                //读取服务器返回的消息
                String msg = reader.readLine();
                System.out.println(msg);

                //检查用户输入是否结束
                if(QUIT.equals(input)){
                    System.out.println("用户输入结束");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer != null){
                try {
                    writer.close();
                    System.out.println("关闭socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
