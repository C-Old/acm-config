package com.zlead.acmconfig.result;

/**
 * @author shipp
 * @descript
 * @create 2019-04-18 17:44
 */
public enum JsonCode {
    /*操作成功*/
    SUCCESS("200","成功"),

    /*操作失败*/
    FAIL("0","失败");

    private JsonCode(String value,String msg){
        this.value = value;
        this.msg = msg;
    }

    public String val(){
        return value;
    }

    public String msg(){
        return msg;
    }


    private String value;
    private String msg;
}
