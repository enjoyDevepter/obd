package com.miyuan.obd;

/**
 * Created by guomin on 2018/6/26.
 */

public class OBDRightInfo {

    private String fm_right;
    private String gc_right;
    private String ty_right;

    public String getFm_right() {
        return fm_right;
    }

    public void setFm_right(String fm_right) {
        this.fm_right = fm_right;
    }

    public boolean iSupportFM() {
        return !"".equals(fm_right) && "1".equals(fm_right);
    }

    public boolean iSupportTire() {
        return !"".equals(ty_right) && "1".equals(ty_right);
    }

    public boolean iSupportCheck() {
        return !"".equals(gc_right) && "1".equals(gc_right);
    }

    public String getGc_right() {
        return gc_right;
    }

    public void setGc_right(String gc_right) {
        this.gc_right = gc_right;
    }

    public String getTy_right() {
        return ty_right;
    }

    public void setTy_right(String ty_right) {
        this.ty_right = ty_right;
    }

    @Override
    public String toString() {
        return "OBDRightInfo{" +
                "fm_right='" + fm_right + '\'' +
                ", gc_right='" + gc_right + '\'' +
                ", ty_right='" + ty_right + '\'' +
                '}';
    }
}
