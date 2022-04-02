package com.nqm.event_manager.models;

import com.nqm.event_manager.utils.StringUtil;

public class Employee implements Comparable<Employee> {
    private String id;
    private String hoTen;
    private String chuyenMon;
    private String cmnd;
    private String ngaySinh;
    private String sdt;
    private String email;

    public Employee() {
    }

    public Employee(String id, String hoTen, String chuyenMon, String cmnd, String ngaySinh, String sdt, String email) {
        this.id = id;
        this.hoTen = hoTen;
        this.chuyenMon = chuyenMon;
        this.cmnd = cmnd;
        this.ngaySinh = ngaySinh;
        this.sdt = sdt;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getChuyenMon() {
        return chuyenMon;
    }

    public void setChuyenMon(String chuyenMon) {
        this.chuyenMon = chuyenMon;
    }

    public String getCmnd() {
        return cmnd;
    }

    public void setCmnd(String cmnd) {
        this.cmnd = cmnd;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", chuyenMon='" + chuyenMon + '\'' +
                ", cmnd='" + cmnd + '\'' +
                ", ngaySinh='" + ngaySinh + '\'' +
                ", sdt='" + sdt + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public int compareTo(Employee o) {
        int lastIndex = hoTen.lastIndexOf(" ");
        String thisName = lastIndex > -1 ? hoTen.substring(lastIndex + 1) : hoTen;
        lastIndex = o.getHoTen().lastIndexOf(" ");
        String oName = lastIndex > -1 ? o.getHoTen().substring(lastIndex + 1) : o.getHoTen();
        if (thisName != null && oName != null) {
//            return (thisName.compareToIgnoreCase(oName));
            return StringUtil.collatorVN.compare(thisName, oName);
        } else {
            return 0;
        }
    }
}
