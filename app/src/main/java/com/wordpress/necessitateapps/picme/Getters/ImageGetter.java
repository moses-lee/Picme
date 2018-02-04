package com.wordpress.necessitateapps.picme.Getters;

/**
 * Created by spotzdevelopment on 2/3/2018.
 */

public class ImageGetter {
    public ImageGetter(){

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ImageGetter(String image) {
        this.image = image;
    }

    private String image;
}
