package com.newegg.ec.cache.app.model;

/**
 * Created by gl49 on 2018/3/22.
 */
public class Response {
    public static final int DEFAULT = 0;
    public static final int INFO = -1;
    public static final int ERROR = 1;
    public static final int WARN = 2;

    private int code;
    private String msg;
    private Object res;

    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(int code, Object o, String msg) {
        this.code = code;
        this.res = o;
        this.msg = msg;
    }

    public static Response Result(int code, Object res) {
        return new Response(code, res, null);
    }

    public static Response Success() {
        return new Response(DEFAULT, "success");
    }

    public static Response Info(String msg) {
        return new Response(INFO, msg);
    }

    public static Response Warn(String msg) {
        return new Response(WARN, msg);
    }

    public static Response Error(String msg) {
        return new Response(ERROR, msg);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getRes() {
        return res;
    }

    public void setRes(Object res) {
        this.res = res;
    }
}
