package com.whosbean.newim.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: yamingdeng
 * Date: 13-12-15
 * Time: 下午4:35
 */
public class ConfigYamlManager {

    private static Map<String, Map<String, Object>> maps = new HashMap<String, Map<String, Object>>();

    private static Logger logger = LoggerFactory.getLogger(ConfigYamlManager.class);

    public static void add(String name, Map<String, Object> map){
        maps.put(name, map);
    }

    public static void set(String name, String key, Object value){
        Map<String, Object> map = maps.get(name);
        if (map!=null){
            map.put(key, value);
        }
    }

    public static Map<String, Object> load(String fileName, String name){
        Map<String, Object> map = maps.get(name);
        if (map == null){
            try {
                map = _doLoad(fileName);
            } catch (IOException e) {
                logger.error("can't load config: " + fileName);
            }
            maps.put(name, map);
        }
        return map;
    }

    private static Map<String, Object> _doLoad(String fileName) throws IOException {
        System.out.println("Config loading. " + fileName);
        ClassPathResource classPathResource = new ClassPathResource(fileName);
        if (classPathResource==null){
            logger.error("can't load config: " + fileName);
            return null;
        }else{
            InputStream input = classPathResource.getInputStream();
            Yaml yaml = new Yaml();
            return  (Map) yaml.load(input);
        }
    }
}
