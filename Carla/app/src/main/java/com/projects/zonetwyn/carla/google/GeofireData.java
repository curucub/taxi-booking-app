package com.projects.zonetwyn.carla.google;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeofireData {

    @SerializedName(".priority")
    @Expose
    private String priority;
    @SerializedName("g")
    @Expose
    private String g;
    @SerializedName("l")
    @Expose
    private List<Double> l = null;

    private String uid;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public List<Double> getL() {
        return l;
    }

    public void setL(List<Double> l) {
        this.l = l;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "GeofireData{" +
                "priority='" + priority + '\'' +
                ", g='" + g + '\'' +
                ", l=" + l +
                '}';
    }
}