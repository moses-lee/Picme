package com.wordpress.necessitateapps.picme.Getters;

/**
 * Created by spotzdevelopment on 2/3/2018.
 */

public class FriendsGetter {
    public FriendsGetter(){

    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public FriendsGetter(String userUID) {
        this.userUID = userUID;
    }

    private String userUID;

}
