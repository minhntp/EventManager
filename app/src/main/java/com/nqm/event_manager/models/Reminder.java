package com.nqm.event_manager.models;

public class Reminder implements Comparable<Reminder> {
    String id;
    String eventId;
    int minute;
    String time;

    public Reminder(String id, String eventId, int minute, String time) {
        this.id = id;
        this.eventId = eventId;
        this.minute = minute;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int compareTo(Reminder o) {
        return minute - o.getMinute();
    }
}
