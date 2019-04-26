package com.zlead.acmconfig.result;

import java.io.Serializable;

/**
 * @author shipp
 * @descript
 * @create 2019-04-18 14:57
 */
public class JsonResult<T> implements Serializable {
    private String code;
    private String info;
    private T data;

    public JsonResult(String code, String info, T data) {
        this.code = code;
        this.info = info;
        this.data = data;
    }

    public JsonResult() {
    }

    @Override
    public String toString() {
        return "JsonResult{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                ", data=" + data +
                '}';
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
