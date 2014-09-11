package com.whosbean.newim.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by yaming_deng on 14-9-11.
 */
@Component
public class AppContextHolder implements ApplicationContextAware, InitializingBean {

    public static AppContextHolder context;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        context = this;
    }

    public <T> T get(Class<T> clazz, String name){
        return this.applicationContext.getBean(name, clazz);
    }

    public <T> T get(Class<T> clazz){
        return this.applicationContext.getBean(clazz);
    }
}
