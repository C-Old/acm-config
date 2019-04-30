package com.zlead.acmconfig.controller;

import com.alibaba.edas.acm.ConfigService;
import com.alibaba.edas.acm.exception.ConfigException;

import com.taobao.diamond.utils.StringUtils;
import com.zlead.acmconfig.entity.RequestParamEntity;
import com.zlead.acmconfig.exception.ParameterNotFoundException;
import com.zlead.acmconfig.result.JsonCode;
import com.zlead.acmconfig.result.JsonResult;
import com.zlead.acmconfig.utils.EnvUtil;
import com.zlead.acmconfig.utils.MD5Util;
import com.zlead.acmconfig.utils.OutDateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * @author shipp
 * @descript  统一获取配置接口
 * @create 2019-04-18 14:44
 */
@RestController
@RequestMapping(value = "/api/acm")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);



    @Value("${endpoint}")
    private String endpoint;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/getConfigResource",method = RequestMethod.POST)
    public JsonResult getConfigResource(@RequestBody Map<String,Object> map) {
        RequestParamEntity requestParamEntity =new RequestParamEntity();
        requestParamEntity.setDataId(map.get("dataId").toString());
        requestParamEntity.setEnv(map.get("env").toString());
        String app_key=String.valueOf(map.get("app_key"));
        requestParamEntity.setApp_key(app_key);
        requestParamEntity.setSign(map.get("sign").toString());
        requestParamEntity.setDate(map.get("date").toString());
        //获取命名空间以及accessKey和secretKey值
        Map envMap = EnvUtil.getEnvConfig(requestParamEntity.getEnv());
        //校验时间戳
        String timestamp = requestParamEntity.getDate();
        boolean timeFlag = checkSignTime(timestamp);

        if(!timeFlag){
            return  new JsonResult(JsonCode.FAIL.val(),JsonCode.FAIL.msg(),null);
        }
        //验证签名
        //requestParamEntity.setEnv(envMap.get("env").toString());
        boolean signFlag = validateSign(requestParamEntity);
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
    // 校验签名前后过期时间
    private boolean checkSignTime(String timestamp) {
        boolean timeFlag = true;
        if (StringUtils.isBlank(timestamp)) {
            logger.error("时间戳缺失");
            timeFlag = false;
        }
        long currentTime = System.currentTimeMillis() / 1000;// 统一都传毫秒
        long requestTime = 0;
        try{
            requestTime = Long.parseLong(timestamp);
        }catch (NumberFormatException  e){
            logger.error("时间戳转换异常");
            timeFlag = false;
        }
        if (Math.abs(currentTime - requestTime) > OutDateUtil.OUT_OF_DATE_TIME_LONG) {
            logger.error("签名已过期，服务器当前时间:{}", currentTime);
            timeFlag = false;
        }
        return timeFlag;
    }

    //验签
    private boolean validateSign(RequestParamEntity requestParamEntity) {
        try{
            //命名空间
            String env = requestParamEntity.getEnv();

            String app_key = requestParamEntity.getApp_key();
            //请求的签名
            String sign = requestParamEntity.getSign();
            //请求时间戳
            String date = requestParamEntity.getDate();
            //配置id
            String dataId = requestParamEntity.getDataId();
            if(!validParamsNotNull(env,app_key,sign,date)){
                return false;
            }
            //RSA256
            //待签名字符串
            Map<String,Object> signData = new HashMap<>();
            //和app端要统一请求参数排列数序
            signData.put("app_key",app_key);
            signData.put("env",env);
            signData.put("dataId",dataId);
            /*signData.put("accessKey",accessKey);
            signData.put("secretKey",secretKey);*/
            signData.put("date",date);
            String src = getStr(signData);
            //System.out.println(src);
            //签名
            //Map<String, byte[]> keyMap = SHA256SignUtil.generateKeyBytes();
            //私钥签名
            //byte[] sign256 = SHA256SignUtil.sign256(src, SHA256SignUtil.restorePrivateKey(keyMap.get(SHA256SignUtil.PRIVATE_KEY)));
            //String sign1 = SHA256SignUtil.encodeBase64(sign256);
           // System.out.println("sign=" + sign);//客户端的sign
            //System.out.println("sign1=" + sign1);//服务端根据请求参数新生成的sign
            //公钥验签
            //boolean result = SHA256SignUtil.verify256(src,SHA256SignUtil.decodeBase64(sign.getBytes()),SHA256SignUtil.getPublicKey(""));
            //MD5验证
            boolean result = MD5Util.verifyMD5(sign,src);
            return  result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 验证参数不为空
     * @param params
     * @throws ParameterNotFoundException
     */
    public static boolean validParamsNotNull(Object... params) throws ParameterNotFoundException {
        boolean flag = true;
        if (params == null || params.length < 1){
            return flag;
        }
        for (Object o:params) {
            if (o == null){
                flag = false;
                break;
            }
            if (o instanceof String){
                if(StringUtils.isBlank((String) o)){
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    //拼接请求参数
    private static String getStr(Map<String,Object> map){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,Object> entry:map.entrySet()){
            String v = entry.getValue() == null ? "" : entry.getValue().toString();
            String k = entry.getKey();
            list.add(k + "=" + v + "&");
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String str = sb.toString();
        return str.substring(0,str.length()-1);
    }

}
