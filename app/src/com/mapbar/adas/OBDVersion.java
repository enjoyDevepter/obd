package com.mapbar.adas;

/**
 * Created by guomin on 2018/6/5.
 */

public class OBDVersion {

    private String status;
    private String message;
    private int updateState;
    private String bVersion;
    private String pVersion;
    private String url;
    private String params;
    private int size;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "OBDVersionInfo{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", updateState=" + updateState +
                ", bVersion='" + bVersion + '\'' +
                ", pVersion='" + pVersion + '\'' +
                ", url='" + url + '\'' +
                ", params='" + params + '\'' +
                ", size=" + size +
                '}';
    }
}
