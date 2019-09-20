package com.projects.zonetwyn.carladriver.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;

@IgnoreExtraProperties
public class Bill {

    @Expose
    private String uid;
    @Expose
    private String createdAt;
    @Expose
    private String status;
    @Expose
    private String pictureUrl;

    @Expose
    private String clientUid;
    @Expose
    private String rideUid;

    private Client client;
    private Ride ride;

    public Bill() {
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

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    public String getRideUid() {
        return rideUid;
    }

    public void setRideUid(String rideUid) {
        this.rideUid = rideUid;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
