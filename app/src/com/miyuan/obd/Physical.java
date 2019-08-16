package com.miyuan.obd;

/**
 * Created by guomin on 2018/6/3.
 */

public class Physical {
    private String name;
    private int count;
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Physical{" +
                "name='" + name + '\'' +
                ", count='" + count + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}


