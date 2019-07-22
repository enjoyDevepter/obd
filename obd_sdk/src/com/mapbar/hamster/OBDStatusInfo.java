package com.mapbar.hamster;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by guomin on 2018/3/7.
 */

public class OBDStatusInfo implements Serializable {
    private String sn;
    private String boxId;
    private String pVersion;
    private String bVersion;
    private int sensitive;
    private boolean berforeMatching;
    private boolean currentMatching;
    private byte[] orginal;
    private int hudType;
    private boolean supportNavi;
    private boolean supportFM;
    private boolean news;

    public byte[] getOrginal() {
        return orginal;
    }

    public void setOrginal(byte[] orginal) {
        this.orginal = orginal;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getpVersion() {
        return pVersion;
    }

    public void setpVersion(String pVersion) {
        this.pVersion = pVersion;
    }

    public String getbVersion() {
        return bVersion;
    }

    public void setbVersion(String bVersion) {
        this.bVersion = bVersion;
    }

    public int getSensitive() {
        return sensitive;
    }

    public void setSensitive(int sensitive) {
        this.sensitive = sensitive;
    }

    public boolean isBerforeMatching() {
        return berforeMatching;
    }

    public void setBerforeMatching(boolean berforeMatching) {
        this.berforeMatching = berforeMatching;
    }

    public boolean isCurrentMatching() {
        return currentMatching;
    }

    public void setCurrentMatching(boolean currentMatching) {
        this.currentMatching = currentMatching;
    }

    public int getHudType() {
        return hudType;
    }

    public void setHudType(int hudType) {
        this.hudType = hudType;
    }

    public boolean isSupportNavi() {
        return supportNavi;
    }

    public void setSupportNavi(boolean supportNavi) {
        this.supportNavi = supportNavi;
    }

    public boolean isSupportFM() {
        return supportFM;
    }

    public void setSupportFM(boolean supportFM) {
        this.supportFM = supportFM;
    }

    public boolean isNews() {
        return news;
    }

    public void setNews(boolean news) {
        this.news = news;
    }

    @Override
    public String toString() {
        return "OBDStatusInfo{" +
                "sn='" + sn + '\'' +
                ", boxId='" + boxId + '\'' +
                ", pVersion='" + pVersion + '\'' +
                ", bVersion='" + bVersion + '\'' +
                ", sensitive=" + sensitive +
                ", berforeMatching=" + berforeMatching +
                ", currentMatching=" + currentMatching +
                ", orginal=" + Arrays.toString(orginal) +
                ", hudType=" + hudType +
                ", supportNavi=" + supportNavi +
                ", supportFM=" + supportFM +
                ", news=" + news +
                '}';
    }
}
