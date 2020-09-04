package com.miyuan.obd;

/**
 * Created by guomin on 2018/6/4.
 */

public class FirmwareUpdateInfo {
    private String status;
    private String message;
    private int bUpdateState;
    private int pUpdateState;
    private int id;
    private String bVersion;
    private String pVersion;
    private String version;
    private String url;
    private int size;
    private String desc;
    private String create_time;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getbUpdateState() {
        return bUpdateState;
    }

    public void setbUpdateState(int bUpdateState) {
        this.bUpdateState = bUpdateState;
    }

    public int getpUpdateState() {
        return pUpdateState;
    }

    public void setpUpdateState(int pUpdateState) {
        this.pUpdateState = pUpdateState;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getbVersion() {
        return bVersion;
    }

    public void setbVersion(String bVersion) {
        this.bVersion = bVersion;
    }

    public String getpVersion() {
        return pVersion;
    }

    public void setpVersion(String pVersion) {
        this.pVersion = pVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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
        return "FirmwareUpdateInfo{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", bUpdateState=" + bUpdateState +
                ", pUpdateState=" + pUpdateState +
                ", id=" + id +
                ", bVersion='" + bVersion + '\'' +
                ", pVersion='" + pVersion + '\'' +
                ", version='" + version + '\'' +
                ", url='" + url + '\'' +
                ", size=" + size +
                ", desc='" + desc + '\'' +
                ", create_time='" + create_time + '\'' +
                '}';
    }
}
