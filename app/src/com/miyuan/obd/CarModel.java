package com.miyuan.obd;

import java.util.ArrayList;

/**
 * Created by guomin on 2018/6/3.
 */

public class CarModel {
    private String name;
    private String id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CarModel{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", styles=" + styles +
                '}';
    }
}


