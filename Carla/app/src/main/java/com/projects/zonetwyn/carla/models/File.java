package com.projects.zonetwyn.carla.models;

public class File {

    private String uid;
    private String createdAt;
    private String type;
    private String url;

    public File() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "File{" +
                "uid='" + uid + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
