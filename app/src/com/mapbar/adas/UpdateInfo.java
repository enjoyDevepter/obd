package com.mapbar.adas;

/**
 * Created by guomin on 2018/6/4.
 */

public class UpdateInfo {
    private int updateState;
    private String name;
    private double version;
    private int size;
    private String desc;
    private String url;
    private String create_time;

    public int getUpdateState() {
        return updateState;
    }

    public void setUpdateState(int updateState) {
        this.updateState = updateState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "updateState=" + updateState +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", size=" + size +
                ", desc='" + desc + '\'' +
                ", create_time='" + create_time + '\'' +
                '}';
    }
}
