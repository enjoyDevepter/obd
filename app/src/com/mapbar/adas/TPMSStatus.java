package com.mapbar.adas;

/**
 * Created by guomin on 2018/6/5.
 */

public class TPMSStatus {

    private String status;
    private String message;
    private int state;

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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "TPMSStatus{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", state=" + state +
                '}';
    }
}
