package com.example.myapplication.HelperClasses.HomeAdapter;

public class FeaturedHelperClass {
    int image;
    String text, description;

    public FeaturedHelperClass(int image, String text, String description) {
        this.image = image;
        this.text = text;
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }
}
