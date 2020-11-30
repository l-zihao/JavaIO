package nio_chatroom.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatServer {

    private static final int DEFAULT_PORT = 8828;

    private static final String QUIT = "quit";

    private static final int BUFFER = 1024;

    private ServerSocketChannel server;

    private Selector selector;

    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);

    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);

    private Charset charset = Charset.forName("UTF-8");

    private int port;

    public ChatServer(){
        this(DEFAULT_PORT);
    }

    public ChatServer(int port){
        this.port = port;
    }

    public synchronized void close(Closeable closeable){
        if(closeable != null){
            try {
                System.out.println("关闭serverSocket");
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false); //确保ServerSocketChannel处于非阻塞状态
            server.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT); //注册监听事件
            System.out.println("启动服务器，监听端口：" + port);

            while(true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    //处理被触发的事件
                    handles(selectionKey);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handles(SelectionKey selectionKey) throws IOException {
        //ACCEPT事件 - 和客户端建立了连接
        if(selectionKey.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel)selectionKey.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("客户端[" + client.socket().getPort() + "]已连接");
        }
        //READ事件 - 客户端发送了消息
        else if(selectionKey.isReadable()){
            SocketChannel client = (SocketChannel)selectionKey.channel();
            String fwdMsg = receive(client);
            if(fwdMsg.isEmpty()){
                //客户端异常
                selectionKey.cancel();
                selector.wakeup();
            }else{
                System.out.println("客户端[" + client.socket().getPort() + "]:" + fwdMsg);
                forwardMessage(client, fwdMsg);

                //检查用户是否退出
                if(readyTOQuit(fwdMsg)){
                    selectionKey.cancel();
                    selector.wakeup();
                    System.out.println("客户端[" + client.socket().getPort() + "]已断开");
                }
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            Channel channel = key.channel();
            if(channel instanceof ServerSocketChannel){
                continue;
            }
            if(key.isValid() && !client.equals(channel)){
                wBuffer.clear();
                wBuffer.put(charset.encode("客户端[" + client.socket().getPort() + "]:" + fwdMsg));
                wBuffer.flip();
                while (wBuffer.hasRemaining()){
                    ((SocketChannel)channel).write(wBuffer);
                }
            }
        }
    }

    public boolean readyTOQuit(String msg){
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(7777);
        chatServer.start();
    }
}
