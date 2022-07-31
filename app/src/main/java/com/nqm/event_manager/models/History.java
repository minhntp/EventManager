package com.nqm.event_manager.models;

import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.time.Instant;

public class History {

    String historyId;
    long editedDateTimeInMillis;
    long dateTimeInMillis;
    String eventName;
    String eventLocation;
    String employeeName;
    String employeeSpeciality;
    HistorySalary oldSalary;
    HistorySalary newSalary;

    public History() {
    }

    public History(String historyId, long editedDateTimeInMillis, long dateTimeInMillis, String eventName,
                   String eventLocation, String employeeName, String employeeSpeciality,
                   HistorySalary oldSalary, HistorySalary newSalary) {
        this.historyId = historyId;
        this.editedDateTimeInMillis = editedDateTimeInMillis;
        this.dateTimeInMillis = dateTimeInMillis;
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.employeeName = employeeName;
        this.employeeSpeciality = employeeSpeciality;
        this.oldSalary = oldSalary;
        this.newSalary = newSalary;
    }

    public static HistorySalary getHistorySalaryFromDatabaseString(String historySalaryString) {
        String[] split = historySalaryString.split(";");
        int amount = Integer.parseInt(split[0]);
        boolean isPaid = Boolean.parseBoolean(split[1]);

        return new HistorySalary(amount, isPaid);
    }

    public History(String historyId, long editedDateTimeInMillis, long dateTimeInMillis, String eventName, String eventLocation, String employeeName,
                   String employeeSpeciality, String oldSalaryString, String newSalaryString) {
        this(historyId, editedDateTimeInMillis, dateTimeInMillis, eventName, eventLocation, employeeName,
                employeeSpeciality, getHistorySalaryFromDatabaseString(oldSalaryString),
                getHistorySalaryFromDatabaseString(newSalaryString));
    }

    public static History newHistory(String oldSalaryId, boolean newIsPaid) {
        Salary oldSalary = SalaryRepository.getInstance().getById(oldSalaryId);
        Event event = EventRepository.getInstance().getEventByEventId(oldSalary.getEventId());
        Employee employee = EmployeeRepository.getInstance().getEmployeeById(oldSalary.getEmployeeId());
        return new History("", Instant.now().toEpochMilli(), oldSalary.getStartMili(),
                event.getTen(), event.getDiaDiem(), employee.getHoTen(), employee.getChuyenMon(),
                new History.HistorySalary(oldSalary.getSalary(), oldSalary.isPaid()),
                new History.HistorySalary(oldSalary.getSalary(), newIsPaid));
    }

    public static History newHistory(String oldSalaryId, int newSalary, boolean newIsPaid) {
        Salary oldSalary = SalaryRepository.getInstance().getById(oldSalaryId);
        Event event = EventRepository.getInstance().getEventByEventId(oldSalary.getEventId());
        Employee employee = EmployeeRepository.getInstance().getEmployeeById(oldSalary.getEmployeeId());
        return new History("", Instant.now().toEpochMilli(), oldSalary.getStartMili(),
                event.getTen(), event.getDiaDiem(), employee.getHoTen(), employee.getChuyenMon(),
                new History.HistorySalary(oldSalary.getSalary(), oldSalary.isPaid()),
                new History.HistorySalary(newSalary, newIsPaid));
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public long getEditedDateTimeInMillis() {
        return editedDateTimeInMillis;
    }

    public void setEditedDateTimeInMillis(long editedDateTimeInMillis) {
        this.editedDateTimeInMillis = editedDateTimeInMillis;
    }

    public long getDateTimeInMillis() {
        return dateTimeInMillis;
    }

    public void setDateTimeInMillis(long dateTimeInMillis) {
        this.dateTimeInMillis = dateTimeInMillis;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeSpeciality() {
        return employeeSpeciality;
    }

    public void setEmployeeSpeciality(String employeeSpeciality) {
        this.employeeSpeciality = employeeSpeciality;
    }

    public HistorySalary getOldSalary() {
        return oldSalary;
    }

    public void setOldSalary(HistorySalary oldSalary) {
        this.oldSalary = oldSalary;
    }

    public HistorySalary getNewSalary() {
        return newSalary;
    }

    public void setNewSalary(HistorySalary newSalary) {
        this.newSalary = newSalary;
    }

    // ---------------------------------------------------------------------------------------------

    static public class HistorySalary {
        int amount;
        boolean isPaid;

        public HistorySalary(int amount, boolean isPaid) {
            this.amount = amount;
            this.isPaid = isPaid;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public boolean isPaid() {
            return isPaid;
        }

        public void setPaid(boolean paid) {
            isPaid = paid;
        }

        @Override
        public String toString() {
            return amount + ";" + isPaid;
        }
    }
}
