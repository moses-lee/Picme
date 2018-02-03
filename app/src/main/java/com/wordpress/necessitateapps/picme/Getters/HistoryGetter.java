package com.wordpress.necessitateapps.picme.Getters;



public class HistoryGetter {

    public HistoryGetter(){

    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public HistoryGetter(String userUID) {
        this.userUID = userUID;
    }

    private String userUID;

}
