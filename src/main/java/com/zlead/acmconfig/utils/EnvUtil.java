package com.zlead.acmconfig.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @date 2019/4/22 16:54
 * 获取不同环境配置下的命名空间以及accessKey和secretKey 工具类
 */
public class EnvUtil {
    /**
     *
     * @param env 环境
     * @return 命名空间以及accessKey和secretKey 集合
     */
    public static Map getEnvConfig(String env) {
        Map<String, String> map = new HashMap();
        try {
            //获取配置文件properties
            Properties properties = new Properties();
            properties.load(new InputStreamReader(Object.class.getResourceAsStream("/env.properties"),"UTF-8"));
            map.put("env", properties.getProperty(env));
            map.put("accessKey", properties.getProperty(env + ".accessKey"));
            map.put("secretKey", properties.getProperty(env + ".secretKey"));
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return map;
    }
}
