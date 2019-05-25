package com.nqm.event_manager.models;

public class Event {
    private String id;
    private String ten;
    private String ngayBatDau;
    private String ngayKetThuc;
    private String gioBatDau;
    private String gioKetThuc;
    private String diaDiem;
    private String ghiChu;

    public Event() {
    }

    public Event(String id, String ten, String ngayBatDau, String ngayKetThuc, String gioBatDau, String gioKetThuc,
                 String diaDiem, String ghiChu) {
        this.id = id;
        this.ten = ten;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.diaDiem = diaDiem;
        this.ghiChu = ghiChu;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(String ngaybatdau) {
        this.ngayBatDau = ngaybatdau;
    }

    public String getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(String ngayketthuc) {
        this.ngayKetThuc = ngayketthuc;
    }

    public String getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(String giobatdau) {
        this.gioBatDau = giobatdau;
    }

    public String getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(String gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
