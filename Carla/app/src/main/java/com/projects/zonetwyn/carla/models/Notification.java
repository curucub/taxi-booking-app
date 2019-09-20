package com.projects.zonetwyn.carla.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {

    public static final String TYPE_REQUEST_ACCEPT = "REQUEST ACCEPT";
    public static final String TYPE_REQUEST_REJECT = "REQUEST REJECT";
    public static final String TYPE_DRIVER_REACH = "DRIVER REACH";
    public static final String TYPE_RIDE_START = "RIDE START";
    public static final String TYPE_RIDE_END = "RIDE END";

    private String uid;
    private String createdAt;
    private String type;

    private String title;
    private String content;
    private String status;
    private String userUid;

    public Notification() {
    }

    protected Notification(Parcel in) {
        uid = in.readString();
        createdAt = in.readString();
        type = in.readString();
        title = in.readString();
        content = in.readString();
        status = in.readString();
        userUid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(createdAt);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(status);
        dest.writeString(userUid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
}
