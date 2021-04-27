package com.example.pettrackerapp;

public class PetEntry {

    String name;
    String type;
    int homeLatitude, homeLongitude;

    public PetEntry(String name, String type){
        this.name = name;
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setHomeLatLng(int lat, int longitude){
        this.homeLatitude = lat;
        this.homeLongitude = longitude;
    }
}
