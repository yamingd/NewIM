package com.whosbean;

import com.whosbean.newim.entity.ChatMessage;

/**
 * Hello world!
 *
 */
public class App 
{

    public static final String BOXID = "123";
    private static ChatClient cc0;
    private static ChatClient cc1;

    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );

        cc0 = new ChatClient("john-1", "ws://localhost:8180/websocket");
        cc0.start();

        cc1 = new ChatClient("john-2", "ws://localhost:8180/websocket");
        cc1.start();

        System.out.println("to join chat room: " + BOXID);

        cc0.join(BOXID);
        cc1.join(BOXID);

        System.out.println("to send message: " + BOXID);

        ChatMessage message = new ChatMessage();
        message.id = BOXID;
        message.body = "Chat Message Body";
        message.sender = "john-1";
        message.receiver = "john-2";
        message.group = 0;
        message.type = ChatMessage.TYPE_TEXT;

        cc0.send(message);

        Thread.sleep(60 * 1000);

    }


}
