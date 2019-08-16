package com.miyuan.obd;

/**
 * Created by guomin on 2018/6/3.
 */
public class FaultCode {
    private String id;
    private String suit;
    private String desc_ch;
    private String desc_en;
    private String system;
    private String detail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public String getDesc_ch() {
        return desc_ch;
    }

    public void setDesc_ch(String desc_ch) {
        this.desc_ch = desc_ch;
    }

    public String getDesc_en() {
        return desc_en;
    }

    public void setDesc_en(String desc_en) {
        this.desc_en = desc_en;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return "FaultCode{" +
                "id='" + id + '\'' +
                ", suit='" + suit + '\'' +
                ", desc_ch='" + desc_ch + '\'' +
                ", desc_en='" + desc_en + '\'' +
                ", system='" + system + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FaultCode code = (FaultCode) o;

        if (id != null ? !id.equals(code.id) : code.id != null) return false;
        if (suit != null ? !suit.equals(code.suit) : code.suit != null) return false;
        if (desc_ch != null ? !desc_ch.equals(code.desc_ch) : code.desc_ch != null) return false;
        if (desc_en != null ? !desc_en.equals(code.desc_en) : code.desc_en != null) return false;
        if (system != null ? !system.equals(code.system) : code.system != null) return false;
        return detail != null ? detail.equals(code.detail) : code.detail == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (suit != null ? suit.hashCode() : 0);
        result = 31 * result + (desc_ch != null ? desc_ch.hashCode() : 0);
        result = 31 * result + (desc_en != null ? desc_en.hashCode() : 0);
        result = 31 * result + (system != null ? system.hashCode() : 0);
        result = 31 * result + (detail != null ? detail.hashCode() : 0);
        return result;
    }
}


