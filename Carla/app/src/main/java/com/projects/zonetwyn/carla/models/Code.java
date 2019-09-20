package com.projects.zonetwyn.carla.models;

public class Code {

    private String uid;
    private String createdAt;
    private String expirationDate;
    private double rate;
    private String value;

    private String clientUid;

    public Code() {
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

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Code{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", rate=" + rate +
                '}';
    }
}
