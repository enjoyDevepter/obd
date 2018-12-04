package com.mapbar.adas;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by guomin on 2018/6/3.
 */
public class Physicaltem implements Comparable, Parcelable {
    public static final Creator<Physicaltem> CREATOR = new Creator<Physicaltem>() {
        @Override
        public Physicaltem createFromParcel(Parcel in) {
            return new Physicaltem(in);
        }

        @Override
        public Physicaltem[] newArray(int size) {
            return new Physicaltem[size];
        }
    };
    private int id;
    private int index;
    private String type;
    private String name;
    private double min;
    private double max;
    private boolean compare;
    private String desc;
    private String high_appearance;
    private String higt_reason;
    private String higt_resolvent;
    private String low_appearance;
    private String low_reason;
    private String low_resolvent;
    private int socre;
    private boolean high;
    private int style;  // 0 正常、1 异常
    private String current;

    public Physicaltem(Parcel in) {
        id = in.readInt();
        index = in.readInt();
        type = in.readString();
        name = in.readString();
        min = in.readDouble();
        max = in.readDouble();
        compare = in.readByte() != 0;
        desc = in.readString();
        high_appearance = in.readString();
        higt_reason = in.readString();
        higt_resolvent = in.readString();
        low_appearance = in.readString();
        low_reason = in.readString();
        low_resolvent = in.readString();
        socre = in.readInt();
        high = in.readByte() != 0;
        style = in.readInt();
        current = in.readString();
    }

    public Physicaltem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public boolean isCompare() {
        return compare;
    }

    public void setCompare(boolean compare) {
        this.compare = compare;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getHigh_appearance() {
        return high_appearance;
    }

    public void setHigh_appearance(String high_appearance) {
        this.high_appearance = high_appearance;
    }

    public String getHigt_reason() {
        return higt_reason;
    }

    public void setHigt_reason(String higt_reason) {
        this.higt_reason = higt_reason;
    }

    public String getHigt_resolvent() {
        return higt_resolvent;
    }

    public void setHigt_resolvent(String higt_resolvent) {
        this.higt_resolvent = higt_resolvent;
    }

    public String getLow_appearance() {
        return low_appearance;
    }

    public void setLow_appearance(String low_appearance) {
        this.low_appearance = low_appearance;
    }

    public String getLow_reason() {
        return low_reason;
    }

    public void setLow_reason(String low_reason) {
        this.low_reason = low_reason;
    }

    public String getLow_resolvent() {
        return low_resolvent;
    }

    public void setLow_resolvent(String low_resolvent) {
        this.low_resolvent = low_resolvent;
    }

    public int getSocre() {
        return socre;
    }

    public void setSocre(int socre) {
        this.socre = socre;
    }

    public boolean isHigh() {
        return high;
    }

    public void setHigh(boolean high) {
        this.high = high;
    }


    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    @Override
    public String toString() {
        return "Physicaltem{" +
                "id=" + id +
                ", index=" + index +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", min=" + min +
                ", max=" + max +
                ", compare=" + compare +
                ", desc='" + desc + '\'' +
                ", high_appearance='" + high_appearance + '\'' +
                ", higt_reason='" + higt_reason + '\'' +
                ", higt_resolvent='" + higt_resolvent + '\'' +
                ", low_appearance='" + low_appearance + '\'' +
                ", low_reason='" + low_reason + '\'' +
                ", low_resolvent='" + low_resolvent + '\'' +
                ", socre=" + socre +
                ", high=" + high +
                ", current='" + current + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return this.style > ((Physicaltem) o).style ? 0 : 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(index);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeDouble(min);
        dest.writeDouble(max);
        dest.writeByte((byte) (compare ? 1 : 0));
        dest.writeString(desc);
        dest.writeString(high_appearance);
        dest.writeString(higt_reason);
        dest.writeString(higt_resolvent);
        dest.writeString(low_appearance);
        dest.writeString(low_reason);
        dest.writeString(low_resolvent);
        dest.writeInt(socre);
        dest.writeByte((byte) (high ? 1 : 0));
        dest.writeInt(style);
        dest.writeString(current);
    }
}


