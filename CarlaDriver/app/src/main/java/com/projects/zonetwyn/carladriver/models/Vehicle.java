package com.projects.zonetwyn.carladriver.models;

public class Vehicle {

    private String uid;
    private String createdAt;
    private String brand;
    private String model;
    private String year;
    private String registrationNumber;

    private String driverUid;

    public Vehicle() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year='" + year + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", driverUid='" + driverUid + '\'' +
                '}';
    }
}
