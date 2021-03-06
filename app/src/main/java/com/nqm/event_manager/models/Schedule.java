package com.nqm.event_manager.models;

public class Schedule {
    private String scheduleId;
    private String eventId;
    private String time;
    private String content;
    private int order;

    public Schedule() {
        scheduleId = "";
        eventId = "";
        time = "";
        content = "";
        order = 0;
    }

    public Schedule(String scheduleId, String eventId, String time, String content, int order) {
        this.scheduleId = scheduleId;
        this.eventId = eventId;
        this.time = time;
        this.content = content;
        this.order = order;
    }

    public Schedule(Schedule s) {
        this.scheduleId = s.getScheduleId();
        this.eventId = s.getEventId();
        this.time = s.getTime();
        this.content = s.getContent();
        this.order = s.getOrder();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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
