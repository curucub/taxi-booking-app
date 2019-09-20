package com.projects.zonetwyn.carladriver.utils;

public class Event {

    public static final int SUBJECT_SIGN_UP_GO_TO_CODE = 100;
    public static final int SUBJECT_SIGN_UP_GO_TO_INFORMATIONS = 101;
    public static final int SUBJECT_SIGN_UP_GO_TO_VEHICLE = 102;
    public static final int SUBJECT_SIGN_UP_GO_TO_DOCUMENTS = 103;
    public static final int SUBJECT_SIGN_UP_GO_TO_ACCEPTANCE = 104;
    public static final int SUBJECT_SIGN_UP_PROCESS = 105;

    public static final int SUBJECT_SIGN_IN_GO_TO_CODE = 106;
    public static final int SUBJECT_SIGN_IN_PROCESS = 107;

    public static final int SUBJECT_DASHBOARD_LOCATION = 108;

    public static final int SUBJECT_MAIN_START_LOCATION_UPDATES = 109;
    public static final int SUBJECT_MAIN_STOP_LOCATION_UPDATES = 110;

    private int subject;
    private Object data;

    public Event() {
    }

    public Event(int subject, Object data) {
        this.subject = subject;
        this.data = data;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

