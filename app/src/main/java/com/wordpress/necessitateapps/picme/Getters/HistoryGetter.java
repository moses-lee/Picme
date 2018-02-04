package com.wordpress.necessitateapps.picme.Getters;



public class HistoryGetter {

    public HistoryGetter(){

    }

    public HistoryGetter(String from, String msg, boolean replied) {
        this.from = from;
        this.msg = msg;
        this.replied = replied;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isReplied() {
        return replied;
    }

    public void setReplied(boolean replied) {
        this.replied = replied;
    }

    private String from, msg;
    private boolean replied;
}
