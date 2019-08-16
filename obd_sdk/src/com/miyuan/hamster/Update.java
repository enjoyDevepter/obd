package com.miyuan.hamster;

/**
 * Created by guomin on 2018/6/4.
 */

public class Update {
    private int index;
    private int status;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Update{" +
                "index=" + index +
                ", status=" + status +
                '}';
    }
}
