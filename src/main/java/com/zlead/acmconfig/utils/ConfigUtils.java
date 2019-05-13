/**
 * @program: acm-config
 * @description:配置中心
 * @author: ytchen
 * @create: 2019-05-13 15:01
 **/
package com.zlead.acmconfig.utils;

import com.alibaba.edas.acm.ConfigService;
import com.alibaba.edas.acm.exception.ConfigException;
import com.zlead.acmconfig.controller.ConfigController;
import com.zlead.acmconfig.entity.RequestParamEntity;
import com.zlead.acmconfig.result.JsonCode;
import com.zlead.acmconfig.result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);



    @Value("${endpoint}")
    private static String endpoint;


    public static JsonResult getConfigResource(@RequestBody Map<String,Object> map) {

        RequestParamEntity requestParamEntity =new RequestParamEntity();
        requestParamEntity.setDataId(map.get("dataId").toString());
        requestParamEntity.setEnv(map.get("env").toString());
        String app_key=String.valueOf(map.get("app_key"));
        requestParamEntity.setApp_key(app_key);
        requestParamEntity.setDate(map.get("date").toString());
        //获取命名空间以及accessKey和secretKey值
        Map envMap = EnvUtil.getEnvConfig(requestParamEntity.getEnv());
        //验证通过之后
        try {
            Properties properties = new Properties();
            //End Point
            properties.put("endpoint", endpoint);
            //命名空间ID
            properties.put("namespace", envMap.get("env"));
            //
            properties.put("accessKey", envMap.get("accessKey"));
            //
            properties.put("secretKey", envMap.get("secretKey"));
            //根据dataId规范获取group分组
            String group = org.apache.commons.lang3.StringUtils.substringBefore(requestParamEntity.getDataId(),":");
            // 通过 ECS 实例 RAM 角色访问 ACM
            // properties.put("ramRoleName", "$ramRoleName");
            // 如果是加密配置，则添加下面两行进行自动解密
            //properties.put("openKMSFilter", true);
            //properties.put("regionId", "$regionId");
            //初始化配置服务
            ConfigService.init(properties);
            // 主动获取配置
            Properties content = ConfigService.getConfig2Properties(requestParamEntity.getDataId(), group, 6000);
            //System.out.println(content);
            if (content != null) {
                JsonResult jsonResult = new JsonResult();
                jsonResult.setCode(JsonCode.SUCCESS.val());
                jsonResult.setInfo(JsonCode.SUCCESS.msg());
                jsonResult.setData(content);
                return jsonResult;
            }
            // 初始化的时候，给配置添加监听，配置变更会回调通知
            /*ConfigService.addListener(dataId, group, new ConfigChangeListener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    //通过异步消息推送
                }
            });*/
            /**
             * 如果配置值的內容为properties格式（key=value），可使用下面监听器。以便一个配置管理多个配置项
             */
            /**
             ConfigService.addListener("${dataId}", "${group}", new PropertiesListener() {
            @Override public void innerReceive(Properties properties) {
            // TODO Auto-generated method stub
            acmProperties = properties;
            System.out.println(properties);
            }
            });
             **/
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        JsonResult jsonResult = new JsonResult();
        jsonResult.setCode(JsonCode.FAIL.val());
        jsonResult.setInfo(JsonCode.FAIL.msg());
        return null;


    }

}
