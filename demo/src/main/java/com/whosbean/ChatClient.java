package com.whosbean;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketByteListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;
import com.whosbean.newim.common.MessageUtil;
import com.whosbean.newim.entity.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yaming on 2014/10/17.
 */
public class ChatClient extends Thread {

    protected Logger logger = null;

    private String url;
    private String uname;
    private AsyncHttpClient c;
    private WebSocket connection;

    private CountDownLatch countDownLatch = null;

    public ChatClient(String name, String url) {
        this.uname = name;
        this.url = url;
        logger = LoggerFactory.getLogger(ChatClient.class.getName()+"."+name);
        AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder().build();
        c = new AsyncHttpClient(cf);
    }

    public void lostConnection(){

    }

    public void close(){
        if(connection.isOpen()){
            System.out.println("closing websocket");
            connection.close();
            connection = null;
            System.out.println("websocket closed");
        }
    }

    public void join(String boxid) throws Exception {
        if (countDownLatch != null){
            countDownLatch.await();
        }

        ChatMessage.Builder chatbox = ChatMessage.newBuilder();
        chatbox.setBoxid(boxid);
        chatbox.setOp(ChatMessage.ChatOp.JOIN);
        chatbox.setSender(this.uname);
        chatbox.setGroup(0);
        chatbox.setMtype(ChatMessage.MessageType.SYNC);
        connection.sendMessage(chatbox.build().toByteArray());
    }

    public void quit(String boxid) throws Exception {
        if (countDownLatch != null){
            countDownLatch.await();
        }

        ChatMessage.Builder chatbox = ChatMessage.newBuilder();
        chatbox.setBoxid(boxid);
        chatbox.setOp(ChatMessage.ChatOp.QUIT);
        chatbox.setSender(this.uname);
        chatbox.setMtype(ChatMessage.MessageType.SYNC);
        connection.sendMessage(chatbox.build().toByteArray());
    }

    public void send(ChatMessage chatMessage) throws Exception {
        if (countDownLatch != null){
            countDownLatch.await();
        }
        logger.info("to send message. msg=" + chatMessage);
        connection.sendMessage(chatMessage.toByteArray());
    }

    @Override
    public void run() {
        try {

            connect();

            logger.info("websocket connected.");
            while (true){
               Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private final WebSocketByteListener webSocketByteListener = new WebSocketByteListener() {

        @Override
        public void onMessage(byte[] message) {
            try {
                ChatMessage chatMessage = MessageUtil.asT(ChatMessage.class, message);
                logger.info("onMessage: {}", chatMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOpen(WebSocket websocket) {
            connection = websocket;
            countDownLatch.countDown();
            countDownLatch = null;
            //byte[] message = null;
            //websocket.sendMessage(message);
            logger.info("onOpen: {}", websocket);
        }

        @Override
        public void onClose(WebSocket websocket) {
            logger.info("onClose: {}", websocket);
            lostConnection();
        }

        @Override
        public void onError(Throwable t) {
            logger.error("onError: ", t);
        }

    };

    public void connect() throws InterruptedException, ExecutionException, IOException {
        countDownLatch = new CountDownLatch(1);
        Cookie cookie = Cookie.newValidCookie("sid", uname, "127.0.0.1", uname, "/", Integer.MAX_VALUE, 0, false, true);
        WebSocket websocket = c.prepareGet(this.url).addCookie(cookie)
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
                        webSocketByteListener).build()).get();
    }
}
