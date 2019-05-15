/**
 * @program: DEMO
 * @description:
 * @author: ytchen
 * @create: 2019-04-28 17:46
 **/
package com.zlead.acmconfig.utils;


import com.alibaba.fastjson.JSONObject;
import com.zlead.acmconfig.entity.RequestParamEntity;
import com.zlead.acmconfig.exception.ParameterNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;



public class comds {

    private static final Logger logger = LoggerFactory.getLogger(comds.class);
    //拼接请求参数
    public static String getStr(Map<String,Object> map){
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

    public static Map<String,Object> getmap(Map<String,Object> parameter,String sign){
        Map envMap = new HashMap();
        boolean flag = checkSignTime(parameter.get("date").toString());
        if(!flag){
            envMap.put("code", JsonCode.FAIL.val());
            envMap.put("info","时间戳验证失败！");
            return envMap;
        }
        String gis= getStr(parameter);
        boolean flags = MD5Util.verifyMD5(sign,gis);
        if(!flags){
            envMap.put("code",JsonCode.FAIL.val());
            envMap.put("info","验证标签失败！");
            return envMap;
        }
        envMap = EnvUtil.getEnvConfig(parameter.get("app_key").toString());

        envMap.put("date",parameter.get("date").toString());
        String src = getStr(envMap);
        String md5Result = MD5Util.string2MD5(src).substring(8,24);
        envMap.put("sign",md5Result);
        return envMap;

    }

    // 校验签名前后过期时间
    public static boolean checkSignTime(String timestamp) {
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
                if(com.taobao.diamond.utils.StringUtils.isBlank((String) o)){
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    //验签
    public static boolean validateSign(RequestParamEntity requestParamEntity) {
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
//            String Qabts=MD5Util.string2MD5(src).substring(8,24);
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

    public static String getval(Map<String,Object> map){

        String Isval=getStr(map);
        String Qabts=MD5Util.string2MD5(Isval).substring(8,24);
        return Qabts;
    }


}
