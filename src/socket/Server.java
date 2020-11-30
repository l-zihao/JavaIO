package socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {

        final String QUIT = "quit";
        final int DEFAULT_PORT = 8828;

        ServerSocket serverSocket = null;

        try {
            //绑定监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("绑定监听端口:" + DEFAULT_PORT);

            while (true) {
                //等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端[" + socket.getPort() + "]已连接");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())
                );

                String msg = null;
                while ((msg = reader.readLine()) != null) {
                    //读取客户端发送的信息
                    System.out.println("客户端[" + socket.getPort() + "]:" + msg);
                    //回复客户端
                    writer.write(msg + "\n");
                    writer.flush();

                    if(QUIT.equals(msg)){
                        System.out.println("客户端[" + socket.getPort() + "]退出");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("关闭serverSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
