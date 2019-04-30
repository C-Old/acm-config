package com.zlead.acmconfig.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author shipp
 * @descript 封装请求参数实体
 * @create 2019-04-19 10:48
 */

@Data
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

}
