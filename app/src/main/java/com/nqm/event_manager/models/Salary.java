package com.nqm.event_manager.models;

public class Salary {
    private String salaryId;
    private String eventId;
    private String employeeId;
    private int salary;
    private boolean paid;

    public Salary() {
    }

    public Salary(String salaryId, String eventId, String employeeId, int salary, boolean paid) {
        this.salaryId = salaryId;
        this.eventId = eventId;
        this.employeeId = employeeId;
        this.salary = salary;
        this.paid = paid;
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
//class SortSalariesByStartDate implements Comparator<Salary> {
//    @Override
//    public int compare(Salary s1, Salary s2)
//    {
//        Date d1 = Calendar.getInstance().getTime();
//        Date d2 = Calendar.getInstance().getTime();
//        try {
//            d1 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s1.getEventId()).getNgayBatDau());
//            d2 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s2.getEventId()).getNgayBatDau());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return d1.compareTo(d2);
//    }
//
//}
