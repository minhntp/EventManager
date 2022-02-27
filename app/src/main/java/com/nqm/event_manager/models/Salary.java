package com.nqm.event_manager.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Salary implements Parcelable {
    private String salaryId;
    private String eventId;
    private String employeeId;
    private int salary;
    private boolean paid;
    private long startMili;
    private long endMili;

    public Salary() {
    }

    public Salary(Salary s) {
        this(s.salaryId, s.eventId, s.employeeId, s.salary, s.paid, s.startMili, s.endMili);
    }

    public Salary(String salaryId, String eventId, String employeeId, int salary, boolean paid) {
        this(salaryId, eventId, employeeId, salary, paid, 0, 0);
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

    @Override
    public int describeContents() {
        return 0;
    }

    protected Salary(Parcel in) {
        salaryId = in.readString();
        eventId = in.readString();
        employeeId = in.readString();
        salary = in.readInt();
        paid = in.readByte() != 0;
        startMili = in.readLong();
        endMili = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(salaryId);
        parcel.writeString(eventId);
        parcel.writeString(employeeId);
        parcel.writeInt(salary);
        parcel.writeByte((byte) (paid ? 1 : 0));
        parcel.writeLong(startMili);
        parcel.writeLong(endMili);
    }


    public static final Creator<Salary> CREATOR = new Creator<Salary>() {
        @Override
        public Salary createFromParcel(Parcel in) {
            return new Salary(in);
        }

        @Override
        public Salary[] newArray(int size) {
            return new Salary[size];
        }
    };

}
