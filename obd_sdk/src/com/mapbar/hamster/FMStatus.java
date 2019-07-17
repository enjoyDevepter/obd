package com.mapbar.hamster;

import java.io.Serializable;

/**
 * Created by guomin on 2018/6/3.
 */
public class FMStatus implements Serializable {
    private boolean enable;
    private int rate;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "FMStatus{" +
                "enable=" + enable +
                ", rate=" + rate +
                '}';
    }
}


