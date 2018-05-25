package com.mapbar.adas.download;

import java.io.Serializable;

/**
 * Created by shisk on 2017/7/19.
 */

public class AppInfoBean implements Serializable {
    //apk下载路径
    private String apk_path;
    //app id
    private String app_id;
    //大小 单位B
    private long byteSize;
    //一句话描述
    private String description;
    //icon
    private String icon_path;
    //image
    private String image_path;
    //md5
    private String md5;
    private String name;
    private String package_name;
    //长度 单位M
    private double size;
    //更新描述
    private String update_desc;
    //更新时间
    private String update_time;
    //版本id
    private String version_id;
    //版本名
    private String version_name;
    //版本号
    private int version_no;

    public String getApk_path() {
        return apk_path;
    }

    public void setApk_path(String apk_path) {
        this.apk_path = apk_path;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public long getByteSize() {
        return byteSize;
    }

    public void setByteSize(long byteSize) {
        this.byteSize = byteSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon_path() {
        return icon_path;
    }

    public void setIcon_path(String icon_path) {
        this.icon_path = icon_path;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getUpdate_desc() {
        return update_desc;
    }

    public void setUpdate_desc(String update_desc) {
        this.update_desc = update_desc;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getVersion_id() {
        return version_id;
    }

    public void setVersion_id(String version_id) {
        this.version_id = version_id;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public int getVersion_no() {
        return version_no;
    }

    public void setVersion_no(int version_no) {
        this.version_no = version_no;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "apk_path='" + apk_path + '\'' +
                ", app_id='" + app_id + '\'' +
                ", byteSize=" + byteSize +
                ", description='" + description + '\'' +
                ", icon_path='" + icon_path + '\'' +
                ", image_path='" + image_path + '\'' +
                ", md5='" + md5 + '\'' +
                ", name='" + name + '\'' +
                ", package_name='" + package_name + '\'' +
                ", size=" + size +
                ", update_desc='" + update_desc + '\'' +
                ", update_time='" + update_time + '\'' +
                ", version_id='" + version_id + '\'' +
                ", version_name='" + version_name + '\'' +
                ", version_no=" + version_no +
                '}';
    }

}
