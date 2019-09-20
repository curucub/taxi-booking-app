package com.projects.zonetwyn.carladriver.models;

public class Ride {

    private String uid;
    private String createdAt;
    private String duration;
    private String distance;
    private double price;
    private String status;
    private String startingPointUid;
    private String arrivalPointUid;
    private String clientUid;
    private String driverUid;

    private Point startingPoint;
    private Point arrivalPoint;
    private Client client;
    private Driver driver;

    public Ride() {
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartingPointUid() {
        return startingPointUid;
    }

    public void setStartingPointUid(String startingPointUid) {
        this.startingPointUid = startingPointUid;
    }

    public String getArrivalPointUid() {
        return arrivalPointUid;
    }

    public void setArrivalPointUid(String arrivalPointUid) {
        this.arrivalPointUid = arrivalPointUid;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }

    public Point getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
    }

    public Point getArrivalPoint() {
        return arrivalPoint;
    }

    public void setArrivalPoint(Point arrivalPoint) {
        this.arrivalPoint = arrivalPoint;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    @Override
    public String toString() {
        return "Ride{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", duration='" + duration + '\'' +
                ", distance='" + distance + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                '}';
    }
}
