package aio_chatroom.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatClient {

    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";

    private static final int DEFAULT_SERVER_PORT = 7777;

    private static final String QUIT = "quit";

    private static final int BUFFER = 1024;

    private String host;

    private int port;

    private SocketChannel client;

    private Selector selector;

    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);

    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);

    private Charset charset = Charset.forName("UTF-8");

    public ChatClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    public ChatClient(){
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    }


    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public void close(Closeable closeable){
        if(closeable != null){
            try {
                System.out.println("关闭socket");
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);

            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(host, port));

            while(true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    handles(selectionKey);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            // 用户正常退出
        } finally {
            close(selector);
        }
    }

    public void send(String msg) throws IOException {
        if(msg.isEmpty()){
           return;
        }

        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()){
            client.write(wBuffer);
        }

        //检查用户是否准备退出
        if(readyToQuit(msg)){
            close(selector);
        }
    }

    private void handles(SelectionKey selectionKey) throws IOException {
        //CONNECT事件 - 连接就绪事件
        if(selectionKey.isConnectable()){
            SocketChannel client = (SocketChannel)selectionKey.channel();
            if(client.isConnectionPending()){
                client.finishConnect();
                //处理用户输入
                new Thread(new UserInputHandler(this)).start();
            }
            client.register(selector, SelectionKey.OP_READ);
        }
        //READ事件 - 服务器转发消息
        else if(selectionKey.isReadable()){
            SocketChannel client = (SocketChannel)selectionKey.channel();
            String msg = receive(client);

            if(msg.isEmpty()){
                //服务器异常
                close(selector);
            }else{
                System.out.println(msg);
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient("127.0.0.1", 7777);
        chatClient.start();
    }
}
