package com.whosbean.newim.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;

/**
 * 配置规范如下:
 * /resources/$env/$sample.yaml
 * /resources/$env/$app/$sample.yaml
 * $env=dev,test,prod
 * $app=具体的业务, 如elasticsearch, db etc.
 * $sample=具体文件名
 * User: yamingdeng
 * Date: 13-11-25
 * Time: 下午9:58
 */
public abstract class AbstractConfig implements InitializingBean{
    protected String cfgFile;
    protected Map<String, Object> cfg = null;
    protected Logger logger = null;

    protected Logger getLogger(){
        if (logger == null){
            logger = LoggerFactory.getLogger(this.getClass());
        }
        return logger;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.cfgFile = this.getConfName();
        Assert.notNull(this.cfgFile, "cfgFile should not be NULL.");
        this.load();
    }

    protected synchronized void load() throws IOException {
        this.cfgFile = String.format("/%s/%s", this.getEnvName(), this.cfgFile);
        if (this.cfg != null){
            return;
        }
        this.cfg = ConfigYamlManager.load(this.cfgFile, this.getConfName());
    }

    public String getEnvName(){
        return "dev";
    }

    public abstract String getConfName();

    public boolean isEnabled(){
        return this.cfg != null;
    }

    public String getCfgFile() {
        return cfgFile;
    }

    public void setCfgFile(String cfgFile) {
        this.cfgFile = cfgFile;
    }

    public void setMap(Map<String, Object> map){
        if (map == null){
            return;
        }
        if (this.cfg == null){
            this.cfg = new HashMap<String, Object>();
        }
        for (String key : map.keySet()){
            this.cfg.put(key, map.get(key));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type, String key){
        Object temp = this.cfg.get(key);
        if(temp == null){
            return null;
        }
        return (T) temp;
    }

    public <T> T get(Class<T> type, String key, T defaultValue){
        Object temp = this.cfg.get(key);
        if(temp == null){
            return defaultValue;
        }
        return (T) temp;
    }

    public class KeyComparable implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            String v1 = Objects.toString(o1);
            String v2 = Objects.toString(o2);
            return v1.compareToIgnoreCase(v2);
        }
    }

    public List<String> getOrderedKeys(){
        List<String> list = new ArrayList<String>(this.cfg.keySet());
        Collections.sort(list, new KeyComparable());
        return list;
    }
}
