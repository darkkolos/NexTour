package com.example.nextour.classes;

import java.io.Serializable;

public class Local implements Serializable {

    static final long serialVersionUID = 1;

    private int id;
    private String morada;
    private String distrito;
    private double latitude;
    private double longitude;

    public Local(int id, String morada, String distrito, double aLatitude, double aLongitude) {
        this.id = id;
        this.morada = morada;
        this.distrito = distrito;
        this.latitude = aLatitude;
        this.longitude = aLongitude;
    }

    public int getId() {
        return id;
    }

    public String getDistrito() {
        return distrito;
    }

    public String getMorada() {
        return morada;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }
}
