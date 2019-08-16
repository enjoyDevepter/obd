package com.miyuan.hamster;

import java.io.Serializable;

/**
 * Created by guomin on 2018/6/3.
 */
public class HUDStatus implements Serializable {
    private int hudType;
    private boolean tempShow;
    private boolean rpmShow;
    private boolean speedShow;
    private boolean oilShow;
    private boolean avgOilShow;
    private boolean remainderOilShow;
    private boolean consumeOilShow;
    private boolean voltageShow;
    private boolean timeShow;
    private boolean mileShow;
    private boolean tireShow;
    private boolean engineloadShow;
    private int multifunctionalOneType;
    private int multifunctionalTwoType;
    private int multifunctionalThreeType;

    public int getHudType() {
        return hudType;
    }

    public void setHudType(int hudType) {
        this.hudType = hudType;
    }

    public boolean isTempShow() {
        return tempShow;
    }

    public void setTempShow(boolean tempShow) {
        this.tempShow = tempShow;
    }

    public boolean isRpmShow() {
        return rpmShow;
    }

    public void setRpmShow(boolean rpmShow) {
        this.rpmShow = rpmShow;
    }

    public boolean isSpeedShow() {
        return speedShow;
    }

    public void setSpeedShow(boolean speedShow) {
        this.speedShow = speedShow;
    }

    public boolean isOilShow() {
        return oilShow;
    }

    public void setOilShow(boolean oilShow) {
        this.oilShow = oilShow;
    }

    public boolean isAvgOilShow() {
        return avgOilShow;
    }

    public void setAvgOilShow(boolean avgOilShow) {
        this.avgOilShow = avgOilShow;
    }

    public boolean isRemainderOilShow() {
        return remainderOilShow;
    }

    public void setRemainderOilShow(boolean remainderOilShow) {
        this.remainderOilShow = remainderOilShow;
    }

    public boolean isConsumeOilShow() {
        return consumeOilShow;
    }

    public void setConsumeOilShow(boolean consumeOilShow) {
        this.consumeOilShow = consumeOilShow;
    }

    public boolean isVoltageShow() {
        return voltageShow;
    }

    public void setVoltageShow(boolean voltageShow) {
        this.voltageShow = voltageShow;
    }

    public boolean isTimeShow() {
        return timeShow;
    }

    public void setTimeShow(boolean timeShow) {
        this.timeShow = timeShow;
    }

    public boolean isMileShow() {
        return mileShow;
    }

    public void setMileShow(boolean mileShow) {
        this.mileShow = mileShow;
    }

    public boolean isTireShow() {
        return tireShow;
    }

    public void setTireShow(boolean tireShow) {
        this.tireShow = tireShow;
    }

    public boolean isEngineloadShow() {
        return engineloadShow;
    }

    public void setEngineloadShow(boolean engineloadShow) {
        this.engineloadShow = engineloadShow;
    }

    public int getMultifunctionalOneType() {
        return multifunctionalOneType;
    }

    public void setMultifunctionalOneType(int multifunctionalOneType) {
        this.multifunctionalOneType = multifunctionalOneType;
    }

    public int getMultifunctionalTwoType() {
        return multifunctionalTwoType;
    }

    public void setMultifunctionalTwoType(int multifunctionalTwoType) {
        this.multifunctionalTwoType = multifunctionalTwoType;
    }

    public int getMultifunctionalThreeType() {
        return multifunctionalThreeType;
    }

    public void setMultifunctionalThreeType(int multifunctionalThreeType) {
        this.multifunctionalThreeType = multifunctionalThreeType;
    }

    @Override
    public String toString() {
        return "HUDStatus{" +
                "hudType=" + hudType +
                ", tempShow=" + tempShow +
                ", rpmShow=" + rpmShow +
                ", speedShow=" + speedShow +
                ", oilShow=" + oilShow +
                ", avgOilShow=" + avgOilShow +
                ", remainderOilShow=" + remainderOilShow +
                ", consumeOilShow=" + consumeOilShow +
                ", voltageShow=" + voltageShow +
                ", timeShow=" + timeShow +
                ", mileShow=" + mileShow +
                ", tireShow=" + tireShow +
                ", engineloadShow=" + engineloadShow +
                ", multifunctionalOneType=" + multifunctionalOneType +
                ", multifunctionalTwoType=" + multifunctionalTwoType +
                ", multifunctionalThreeType=" + multifunctionalThreeType +
                '}';
    }
}


