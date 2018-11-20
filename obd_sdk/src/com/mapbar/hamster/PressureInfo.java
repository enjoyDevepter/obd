package com.mapbar.hamster;

import java.io.Serializable;

/**
 * Created by guomin on 2018/6/3.
 */
public class PressureInfo implements Serializable {

    /**
     * 0 正常   1 左前  2 右前  3 左后  4 右后
     */
    private int status;
    private int faultCount;
    private int speed;
    private float voltage;
    private int temperature;
    private int rotationRate;
    private double oilConsumption;
    private double consumption;
    private int surplusOil;
    private boolean update;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getRotationRate() {
        return rotationRate;
    }

    public void setRotationRate(int rotationRate) {
        this.rotationRate = rotationRate;
    }

    public double getOilConsumption() {
        return oilConsumption;
    }

    public void setOilConsumption(double oilConsumption) {
        this.oilConsumption = oilConsumption;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public int getSurplusOil() {
        return surplusOil;
    }

    public void setSurplusOil(int surplusOil) {
        this.surplusOil = surplusOil;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "PressureInfo{" +
                "status=" + status +
                ", faultCount=" + faultCount +
                ", speed=" + speed +
                ", voltage=" + voltage +
                ", temperature=" + temperature +
                ", rotationRate=" + rotationRate +
                ", oilConsumption=" + oilConsumption +
                ", consumption=" + consumption +
                ", surplusOil=" + surplusOil +
                ", update=" + update +
                '}';
    }
}


