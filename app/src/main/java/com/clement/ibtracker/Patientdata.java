package com.clement.ibtracker;

public class Patientdata {
    private String uuid;
    private String situation;
    private String date;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Patientdata String{" +
                "uuid='" + uuid + '\'' +
                ", situation='" + situation + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
