package com.wordpress.necessitateapps.picme.Getters;

/**
 * Created by spotzdevelopment on 2/3/2018.
 */

public class RequestGetter {

    public RequestGetter(){

    }

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public RequestGetter(String msg, String from) {
        this.msg = msg;
        this.from = from;
    }

    private String from;
}
