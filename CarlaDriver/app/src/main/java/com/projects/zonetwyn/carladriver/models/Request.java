package com.projects.zonetwyn.carladriver.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Request implements Parcelable {

    private String uid;
    private String createdAt;
    private String duration;
    private String distance;
    private double price;
    private String status;
    private String comment;
    private String startingPointUid;
    private String arrivalPointUid;
    private String clientUid;
    private String driverUid;

    private Point startingPoint;
    private Point arrivalPoint;
    private Client client;
    private Driver driver;

    public Request() {
    }

    protected Request(Parcel in) {
        uid = in.readString();
        createdAt = in.readString();
        duration = in.readString();
        distance = in.readString();
        price = in.readDouble();
        status = in.readString();
        comment = in.readString();
        startingPointUid = in.readString();
        arrivalPointUid = in.readString();
        clientUid = in.readString();
        driverUid = in.readString();
        startingPoint = in.readParcelable(Point.class.getClassLoader());
        arrivalPoint = in.readParcelable(Point.class.getClassLoader());
        client = in.readParcelable(Client.class.getClassLoader());
        driver = in.readParcelable(Driver.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(createdAt);
        dest.writeString(duration);
        dest.writeString(distance);
        dest.writeDouble(price);
        dest.writeString(status);
        dest.writeString(comment);
        dest.writeString(startingPointUid);
        dest.writeString(arrivalPointUid);
        dest.writeString(clientUid);
        dest.writeString(driverUid);
        dest.writeParcelable(startingPoint, flags);
        dest.writeParcelable(arrivalPoint, flags);
        dest.writeParcelable(client, flags);
        dest.writeParcelable(driver, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        return "Request{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", duration='" + duration + '\'' +
                ", distance='" + distance + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                '}';
    }
}
