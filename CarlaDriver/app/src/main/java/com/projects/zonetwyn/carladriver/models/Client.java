package com.projects.zonetwyn.carladriver.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Client implements Parcelable  {

    private String uid;
    private String createdAt;
    private String name;
    private String surname;
    private String phone;
    private String email;
    private String homeAddress;
    private String officeAddress;
    private boolean online;
    private String pictureUrl;

    private String cardHolder;
    private String cardNumber;
    private String cardCode;
    private String cardExpirationDate;

    private String token;

    public Client() {
    }

    protected Client(Parcel in) {
        uid = in.readString();
        createdAt = in.readString();
        name = in.readString();
        surname = in.readString();
        phone = in.readString();
        email = in.readString();
        homeAddress = in.readString();
        officeAddress = in.readString();
        online = in.readByte() != 0;
        pictureUrl = in.readString();
        cardHolder = in.readString();
        cardNumber = in.readString();
        cardCode = in.readString();
        cardExpirationDate = in.readString();
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
        dest.writeString(homeAddress);
        dest.writeString(officeAddress);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeString(pictureUrl);
        dest.writeString(cardHolder);
        dest.writeString(cardNumber);
        dest.writeString(cardCode);
        dest.writeString(cardExpirationDate);
        dest.writeString(token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
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

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public String getCardExpirationDate() {
        return cardExpirationDate;
    }

    public void setCardExpirationDate(String cardExpirationDate) {
        this.cardExpirationDate = cardExpirationDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Client{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
