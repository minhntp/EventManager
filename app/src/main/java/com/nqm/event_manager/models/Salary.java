package com.nqm.event_manager.models;

public class Salary {
    private String salaryId;
    private String eventId;
    private String employeeId;
    private int salary;
    private boolean paid;
    private long startMili;
    private long endMili;

    public Salary() {
    }

    public Salary(String salaryId, String eventId, String employeeId, int salary, boolean paid) {
        this.salaryId = salaryId;
        this.eventId = eventId;
        this.employeeId = employeeId;
        this.salary = salary;
        this.paid = paid;
        this.startMili = 0;
        this.endMili = 0;
    }

    public Salary(String salaryId, String eventId, String employeeId, int salary, boolean paid,
                  long startMili, long endMili) {
        this.salaryId = salaryId;
        this.eventId = eventId;
        this.employeeId = employeeId;
        this.salary = salary;
        this.paid = paid;
        this.startMili = startMili;
        this.endMili = endMili;
    }

    public long getStartMili() {
        return startMili;
    }

    public void setStartMili(long startMili) {
        this.startMili = startMili;
    }

    public long getEndMili() {
        return endMili;
    }

    public void setEndMili(long endMili) {
        this.endMili = endMili;
    }

    public String getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(String salaryId) {
        this.salaryId = salaryId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
