package com.projects.zonetwyn.carla.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Pricing implements Parcelable {

    private String uid;
    private String createdAt;
    private String type;
    private double base;
    private double perKilometer;
    private double perMinute;
    private String startTime;
    private String endTime;

    public Pricing() {
    }

    protected Pricing(Parcel in) {
        uid = in.readString();
        createdAt = in.readString();
        type = in.readString();
        base = in.readDouble();
        perKilometer = in.readDouble();
        perMinute = in.readDouble();
        startTime = in.readString();
        endTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(createdAt);
        dest.writeString(type);
        dest.writeDouble(base);
        dest.writeDouble(perKilometer);
        dest.writeDouble(perMinute);
        dest.writeString(startTime);
        dest.writeString(endTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Pricing> CREATOR = new Creator<Pricing>() {
        @Override
        public Pricing createFromParcel(Parcel in) {
            return new Pricing(in);
        }

        @Override
        public Pricing[] newArray(int size) {
            return new Pricing[size];
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getBase() {
        return base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public double getPerKilometer() {
        return perKilometer;
    }

    public void setPerKilometer(double perKilometer) {
        this.perKilometer = perKilometer;
    }

    public double getPerMinute() {
        return perMinute;
    }

    public void setPerMinute(double perMinute) {
        this.perMinute = perMinute;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
