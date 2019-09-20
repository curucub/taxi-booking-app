package com.projects.zonetwyn.carladriver.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Driver implements Parcelable {

    private String uid;
    private String createdAt;
    private String name;
    private String surname;
    private String phone;
    private String email;
    private String address;
    private boolean online;
    private boolean activated;

    private String pictureUrl;

    private int note;
    private int ridesCount;
    private double earningsDaily;
    private double earningsWeekly;

    private double latitude;
    private double longitude;

    private String vehiculeUid;

    private String token;

    public Driver() {
    }

    protected Driver(Parcel in) {
        uid = in.readString();
        createdAt = in.readString();
        name = in.readString();
        surname = in.readString();
        phone = in.readString();
        email = in.readString();
        address = in.readString();
        online = in.readByte() != 0;
        activated = in.readByte() != 0;
        pictureUrl = in.readString();
        note = in.readInt();
        ridesCount = in.readInt();
        earningsDaily = in.readDouble();
        earningsWeekly = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
        token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(createdAt);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(address);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeByte((byte) (activated ? 1 : 0));
        dest.writeString(pictureUrl);
        dest.writeInt(note);
        dest.writeInt(ridesCount);
        dest.writeDouble(earningsDaily);
        dest.writeDouble(earningsWeekly);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel in) {
            return new Driver(in);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

    public static Driver getInitializedDriver() {
        Driver driver = new Driver();
        driver.setActivated(false);
        driver.setOnline(false);
        driver.setNote(0);
        driver.setRidesCount(0);
        driver.setEarningsDaily(0);
        driver.setEarningsWeekly(0);
        return driver;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public int getRidesCount() {
        return ridesCount;
    }

    public void setRidesCount(int ridesCount) {
        this.ridesCount = ridesCount;
    }

    public double getEarningsDaily() {
        return earningsDaily;
    }

    public void setEarningsDaily(double earningsDaily) {
        this.earningsDaily = earningsDaily;
    }

    public double getEarningsWeekly() {
        return earningsWeekly;
    }

    public void setEarningsWeekly(double earningsWeekly) {
        this.earningsWeekly = earningsWeekly;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVehiculeUid() {
        return vehiculeUid;
    }

    public void setVehiculeUid(String vehiculeUid) {
        this.vehiculeUid = vehiculeUid;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
