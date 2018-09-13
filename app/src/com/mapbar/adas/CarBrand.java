package com.mapbar.adas;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by guomin on 2018/6/3.
 */

public class CarBrand implements Comparable<CarBrand> {

    private String letter;
    private String name;
    private ArrayList<CarModel> models;
    private String rawName;             // raw
    private String pinyinName;          // filter
    private String sortLetters;         // sort
    private String id;
    private boolean choice;


    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CarModel> getModels() {
        return models;
    }

    public void setModels(ArrayList<CarModel> models) {
        this.models = models;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    public String getPinyinName() {
        return pinyinName;
    }

    public void setPinyinName(String pinyinName) {
        this.pinyinName = pinyinName;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
    }

    @Override
    public String toString() {
        return "CarInfo{" +
                "letter='" + letter + '\'' +
                ", name='" + name + '\'' +
                ", models=" + models +
                ", rawName='" + rawName + '\'' +
                ", pinyinName='" + pinyinName + '\'' +
                ", sortLetters='" + sortLetters + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull CarBrand another) {
        if (sortLetters.startsWith("#")) {
            return 1;
        } else if (another.getSortLetters().startsWith("#")) {
            return -1;
        } else {
            return sortLetters.compareTo(another.getSortLetters());
        }
    }
}


