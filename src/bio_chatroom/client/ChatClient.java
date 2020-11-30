package bio_chatroom.client;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private String DEFAULT_SERVER_HOST = "127.0.0.1";

    private int DEFAULT_SERVER_PORT = 8828;

    private final String QUIT = "quit";

    private Socket socket;

    private BufferedWriter writer;

    private BufferedReader reader;

    //发送消息
    public void send(String msg) throws IOException {
        if(!socket.isOutputShutdown()){
            writer.write(msg + "\n");
            writer.flush();
        }
    }

    //接收服务器消息
    public String receive() throws IOException {
        String msg = null;
        if(!socket.isInputShutdown()){
            msg = reader.readLine();
        }
        return msg;
    }

    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public void close(){
        if(writer != null){
            try {
                System.out.println("关闭socket");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            //创建socket
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);

            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            //处理用户输入
            new Thread(new UserInputHandler(this)).start();

            //读取服务器转发的消息
            String msg = null;
            while((msg = receive()) != null){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
