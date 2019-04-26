package com.zlead.acmconfig.entity;

import java.io.Serializable;

/**
 * @author shipp
 * @descript 封装请求参数实体
 * @create 2019-04-19 10:48
 */
public class RequestParamEntity implements Serializable {
    //命名空间
    private String env;
    //配置集 ID
    private String dataId;
    //分配给应用的app_key
    private String app_key;
    //API输入参数签名结果
    private String sign;
    //时间戳 统一传毫秒
    private String date;


    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
