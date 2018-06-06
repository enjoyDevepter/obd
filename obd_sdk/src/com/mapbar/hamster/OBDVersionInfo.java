package com.mapbar.hamster;

/**
 * Created by guomin on 2018/6/5.
 */

public class OBDVersionInfo {

    private String sn;
    private String version;
    private String car_no;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCar_no() {
        return car_no;
    }

    public void setCar_no(String car_no) {
        this.car_no = car_no;
    }

    @Override
    public String toString() {
        return "OBDVersionInfo{" +
                "sn='" + sn + '\'' +
                ", version='" + version + '\'' +
                ", car_no='" + car_no + '\'' +
                '}';
    }
}
