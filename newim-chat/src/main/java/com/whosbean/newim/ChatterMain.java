package com.whosbean.newim;

import com.whosbean.newim.chatter.ChatterConfig;
import com.whosbean.newim.server.ServerStarter;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChatterMain implements ServerStarter {

    public class ServerThread extends Thread {

        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(600 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 启动推送服务 8080端口
     */
    public void start() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-chatter.xml");
        ChatterConfig prop = context.getBean("chatterConfig", ChatterConfig.class);
        Assert.notNull(prop, "chatterConfig bean is NULL.");
        new ServerThread().start();
        System.out.println("Chatter Node start.");
    }

    /**
     * 入口
     *
     * @param args
     */
    public static void main(String[] args) {
        new ChatterMain().start();
    }
}