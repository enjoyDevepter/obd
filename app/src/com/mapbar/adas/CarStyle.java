package com.mapbar.adas;

/**
 * Created by guomin on 2018/6/3.
 */
public class CarStyle {

    private String name;
    private String id;
    private boolean choice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
    }

    @Override
    public String toString() {
        return "CarStyle{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}


