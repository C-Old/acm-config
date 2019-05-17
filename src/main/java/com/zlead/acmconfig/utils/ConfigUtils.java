
package com.zlead.acmconfig.utils;

import com.alibaba.edas.acm.ConfigService;
import com.alibaba.edas.acm.exception.ConfigException;
import com.zlead.acmconfig.entity.RequestParamEntity;
import com.zlead.acmconfig.result.JsonCode;
import com.zlead.acmconfig.result.JsonResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;


/**
 * @program: acm-config
 * @description:配置中心
 * @author: ytchen
 * @create: 2019-05-13 15:01
 **/
@Component
public class ConfigUtils {

//    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);



    private static  String endpoint;

    @Value("${endpoint}")
    public void setEnv(String endpoint) {
        ConfigUtils.endpoint = endpoint;
    }


    public static JsonResult getConfigResource(@RequestBody Map<String,Object> map) {

        RequestParamEntity requestParamEntity =new RequestParamEntity();
        requestParamEntity.setDataId(map.get("dataId").toString());
        requestParamEntity.setEnv(map.get("env").toString());
        requestParamEntity.setDate(String.valueOf(System.currentTimeMillis() / 1000));
        //获取命名空间以及accessKey和secretKey值
        Map envMap = EnvUtil.getEnvConfig(requestParamEntity.getEnv());
        map.put("app_key",envMap.get("app_key"));
        map.put("date",String.valueOf(System.currentTimeMillis() / 1000));
        requestParamEntity.setApp_key(String.valueOf(envMap.get("app_key")));
        requestParamEntity.setSign(comds.getval(map));
//        map.put("sign",comds.getval(map));
        //校验时间戳
        String timestamp = requestParamEntity.getDate();
        boolean timeFlag = comds.checkSignTime(timestamp);

        if(!timeFlag){
            return  new JsonResult(JsonCode.FAIL.val(),JsonCode.FAIL.msg(),null);
        }
        //验证签名
        //requestParamEntity.setEnv(envMap.get("env").toString());
        boolean signFlag = comds.validateSign(requestParamEntity);
        if(!signFlag){
            return  new JsonResult(JsonCode.FAIL.val(),JsonCode.FAIL.msg(),null);
        }

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