package com.whosbean.newim.chatter;

import com.whosbean.newim.server.ServerStarter;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class Main implements ServerStarter {

    private static int port = 8080;

    public class ServerThread extends Thread {

        @Override
        public void run() {

        }
    }

    /**
     * 启动推送服务 8080端口
     */
    public void start() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-chatter.xml");
        ChatterConfig prop = context.getBean("chatterConfig", ChatterConfig.class);
        Assert.notNull(prop, "chatterConfig bean is NULL.");
        System.out.println("Chatter Node start.");
    }

    /**
     * 入口
     *
     * @param args
     */
    public static void main(String[] args) {
        new Main().start();
    }
}