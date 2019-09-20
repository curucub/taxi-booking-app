package com.projects.zonetwyn.carla.models;

public class Payment {

    private String uid;
    private String createdAt;
    private double amount;
    private String status;
    private String pictureUrl;

    private String driverUid;

    public Payment() {
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getDriverUrl() {
        return driverUid;
    }

    public void setDriverUrl(String driverUid) {
        this.driverUid = driverUid;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", amount=" + amount +
                '}';
    }
}