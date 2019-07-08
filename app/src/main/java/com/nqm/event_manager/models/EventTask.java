package com.nqm.event_manager.models;

public class EventTask {
    private String id;
    private String eventId;
    private String date;
    private String time;
    private String content;
    private boolean isDone;
    private int order;

    public EventTask() {
        this.id = "";
        this.eventId = "";
        this.date = "";
        this.time = "";
        this.content = "";
        isDone = false;
        this.order = 0;
    }

    public EventTask(String id, String eventId, String date, String time, String content, boolean isDone, int order) {
        this.id = id;
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.content = content;
        this.isDone = isDone;
        this.order = order;
    }

    public EventTask(EventTask t) {
        this.id = t.id;
        this.eventId = t.eventId;
        this.date = t.date;
        this.time = t.time;
        this.content = t.content;
        this.isDone = t.isDone;
        this.order = t.order;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getOrder() {
        return order;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
