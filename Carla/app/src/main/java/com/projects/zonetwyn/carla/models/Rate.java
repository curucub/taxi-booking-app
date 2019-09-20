package com.projects.zonetwyn.carla.models;

public class Rate {

    private String uid;
    private String updatedAt;
    private int rate;

    public Rate() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "Rate{" +
                "uid='" + uid + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", rate=" + rate +
                '}';
    }
}
