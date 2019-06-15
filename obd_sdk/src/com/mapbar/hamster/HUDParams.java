package com.mapbar.hamster;

import java.io.Serializable;

/**
 * Created by guomin on 2018/6/3.
 */
public class HUDParams implements Serializable {
    private int light;
    private int volume;
    private int driveTime;
    private int tempWarm;
    private boolean sound;
    private int start;
    private boolean sleep;
    private int speedCalibration;
    private int overSpeed;
    private boolean highMode;
    private int mileCalibration;
    private boolean naviMode;

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getDriveTime() {
        return driveTime;
    }

    public void setDriveTime(int driveTime) {
        this.driveTime = driveTime;
    }

    public int getTempWarm() {
        return tempWarm;
    }

    public void setTempWarm(int tempWarm) {
        this.tempWarm = tempWarm;
    }


    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    public boolean isSleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public int getSpeedCalibration() {
        return speedCalibration;
    }

    public void setSpeedCalibration(int speedCalibration) {
        this.speedCalibration = speedCalibration;
    }

    public int getOverSpeed() {
        return overSpeed;
    }

    public void setOverSpeed(int overSpeed) {
        this.overSpeed = overSpeed;
    }

    public boolean isHighMode() {
        return highMode;
    }

    public void setHighMode(boolean highMode) {
        this.highMode = highMode;
    }

    public int getMileCalibration() {
        return mileCalibration;
    }

    public void setMileCalibration(int mileCalibration) {
        this.mileCalibration = mileCalibration;
    }

    public boolean isNaviMode() {
        return naviMode;
    }

    public void setNaviMode(boolean naviMode) {
        this.naviMode = naviMode;
    }

    @Override
    public String toString() {
        return "HUDParams{" +
                "light=" + light +
                ", volume=" + volume +
                ", driveTime=" + driveTime +
                ", tempWarm=" + tempWarm +
                ", sound=" + sound +
                ", start=" + start +
                ", sleep=" + sleep +
                ", speedCalibration=" + speedCalibration +
                ", overSpeed=" + overSpeed +
                ", highMode=" + highMode +
                ", mileCalibration=" + mileCalibration +
                ", naviMode=" + naviMode +
                '}';
    }
}


