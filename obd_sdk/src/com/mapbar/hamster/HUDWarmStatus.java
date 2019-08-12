package com.mapbar.hamster;

import java.io.Serializable;
import java.util.Arrays;

public class HUDWarmStatus implements Serializable {
    private boolean temperatureWarmShow;
    private boolean voltageWarmShow;
    private boolean oilWarmShow;
    private boolean speedWarmShow;
    private boolean trieWarmShow;
    private boolean tiredWarmShow;
    private boolean faultWarmShow;
    private byte[] origin;


    public boolean isVoltageWarmShow() {
        return voltageWarmShow;
    }

    public void setVoltageWarmShow(boolean voltageWarmShow) {
        this.voltageWarmShow = voltageWarmShow;
    }

    public boolean isFaultWarmShow() {
        return faultWarmShow;
    }

    public void setFaultWarmShow(boolean faultWarmShow) {
        this.faultWarmShow = faultWarmShow;
    }

    public boolean isTemperatureWarmShow() {
        return temperatureWarmShow;
    }

    public void setTemperatureWarmShow(boolean temperatureWarmShow) {
        this.temperatureWarmShow = temperatureWarmShow;
    }

    public boolean isTiredWarmShow() {
        return tiredWarmShow;
    }

    public void setTiredWarmShow(boolean tiredWarmShow) {
        this.tiredWarmShow = tiredWarmShow;
    }

    public boolean isOilWarmShow() {
        return oilWarmShow;
    }

    public void setOilWarmShow(boolean oilWarmShow) {
        this.oilWarmShow = oilWarmShow;
    }

    public boolean isSpeedWarmShow() {
        return speedWarmShow;
    }

    public void setSpeedWarmShow(boolean speedWarmShow) {
        this.speedWarmShow = speedWarmShow;
    }

    public boolean isTrieWarmShow() {
        return trieWarmShow;
    }

    public void setTrieWarmShow(boolean trieWarmShow) {
        this.trieWarmShow = trieWarmShow;
    }

    public byte[] getOrigin() {
        return origin;
    }

    public void setOrigin(byte[] origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "HUDWarmStatus{" +
                "temperatureWarmShow=" + temperatureWarmShow +
                ", voltageWarmShow=" + voltageWarmShow +
                ", oilWarmShow=" + oilWarmShow +
                ", speedWarmShow=" + speedWarmShow +
                ", trieWarmShow=" + trieWarmShow +
                ", tiredWarmShow=" + tiredWarmShow +
                ", faultWarmShow=" + faultWarmShow +
                ", origin=" + Arrays.toString(origin) +
                '}';
    }
}
