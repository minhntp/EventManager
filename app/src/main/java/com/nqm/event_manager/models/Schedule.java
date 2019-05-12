package com.nqm.event_manager.models;

public class Schedule {
    private String scheduleId;
    private String eventId;
    private String time;
    private String content;

    public Schedule() {
        scheduleId = "";
        eventId = "";
        time = "";
        content = "";
    }

    public Schedule(String scheduleId, String eventId, String time, String content) {
        this.scheduleId = scheduleId;
        this.eventId = eventId;
        this.time = time;
        this.content = content;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
