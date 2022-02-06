package com.example.myapplication;

import java.util.Date;

public class Model {
    private String imageUri;
    private String Lat;
    private String Long;
    private Date timeStamp;

    public Model(){

    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLong() {
        return Long;
    }

    public void setLong(String aLong) {
        Long = aLong;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Model(String imageUri, String Lat, String Long, Date timeStamp){
        this.imageUri = imageUri;
        this.Lat = Lat;
        this.Long = Long;
        this.timeStamp = timeStamp;
    }
}
