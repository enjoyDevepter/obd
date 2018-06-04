package com.mapbar.adas;

import java.util.ArrayList;

/**
 * Created by guomin on 2018/6/3.
 */

public class CarModel {
    private String name;
    private ArrayList<CarStyle> styles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CarStyle> getStyles() {
        return styles;
    }

    public void setStyles(ArrayList<CarStyle> styles) {
        this.styles = styles;
    }

    @Override
    public String toString() {
        return "CarModel{" +
                "name='" + name + '\'' +
                ", styles=" + styles +
                '}';
    }
}


