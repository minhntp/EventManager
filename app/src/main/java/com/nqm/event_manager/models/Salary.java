package com.nqm.event_manager.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Salary implements Parcelable {
    String id;
    String eventId;
    String employeeId;
    int salary;
    boolean paid;
    long startMili;
    long endMili;
    boolean isChecked;
    int editedSalary;

    public Salary() {
        this.isChecked = false;
    }

    public Salary(Salary s) {
        this(s.id, s.eventId, s.employeeId, s.salary, s.paid, s.startMili, s.endMili, s.isChecked, s.editedSalary);
    }

    public Salary(String id, String eventId, String employeeId, int salary, boolean paid) {
        this(id, eventId, employeeId, salary, paid, 0, 0, false, salary);
    }

    public Salary(String id, String eventId, String employeeId, int salary, boolean paid,
                  long startMili, long endMili) {
        this(id, eventId, employeeId, salary, paid, startMili, endMili, false, salary);
    }

    public Salary(String id, String eventId, String employeeId, int salary, boolean paid,
                  long startMili, long endMili, boolean isChecked, int editedSalary) {
        this.id = id;
        this.eventId = eventId;
        this.employeeId = employeeId;
        this.salary = salary;
        this.paid = paid;
        this.startMili = startMili;
        this.endMili = endMili;
        this.isChecked = isChecked;
        this.editedSalary = editedSalary;
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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setEditedSalary(int editedSalary) {
        this.editedSalary = editedSalary;
    }

    public int getEditedSalary() {
        return editedSalary;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Salary(Parcel in) {
        id = in.readString();
        eventId = in.readString();
        employeeId = in.readString();
        salary = in.readInt();
        paid = in.readByte() != 0;
        startMili = in.readLong();
        endMili = in.readLong();
        isChecked = in.readByte() != 0;
        editedSalary = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(eventId);
        parcel.writeString(employeeId);
        parcel.writeInt(salary);
        parcel.writeByte((byte) (paid ? 1 : 0));
        parcel.writeLong(startMili);
        parcel.writeLong(endMili);
        parcel.writeByte((byte) (paid ? 1 : 0));
        parcel.writeInt(editedSalary);
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
