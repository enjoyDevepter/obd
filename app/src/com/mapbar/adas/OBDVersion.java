package com.mapbar.adas;

/**
 * Created by guomin on 2018/6/5.
 */

public class OBDVersion {

    private String status;
    private String message;
    private int updateState;
    private int bUpdateState;
    private int pUpdateState;
    private double version;
    private int size;
    private String params;
    private String create_time;
    private boolean upload;

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

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

    public int getUpdateState() {
        return updateState;
    }

    public void setUpdateState(int updateState) {
        this.updateState = updateState;
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

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "OBDVersion{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", updateState=" + updateState +
                ", bUpdateState=" + bUpdateState +
                ", pUpdateState=" + pUpdateState +
                ", version=" + version +
                ", size=" + size +
                ", params='" + params + '\'' +
                ", create_time='" + create_time + '\'' +
                '}';
    }
}
