package com.miyuan.obd;

/**
 * Created by guomin on 2018/6/4.
 */

public class FirmwareUpdateInfo {
    private int updateState;
    private String version;
    private String url;
    private int size;
    private String desc;
    private String create_time;
    private String message;

    public int getUpdateState() {
        return updateState;
    }

    public void setUpdateState(int updateState) {
        this.updateState = updateState;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "FirmwareUpdateInfo{" +
                "updateState=" + updateState +
                ", version='" + version + '\'' +
                ", url='" + url + '\'' +
                ", size=" + size +
                ", desc='" + desc + '\'' +
                ", create_time='" + create_time + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
