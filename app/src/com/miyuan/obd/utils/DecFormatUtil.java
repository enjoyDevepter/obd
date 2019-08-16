package com.miyuan.obd.utils;

import java.text.DecimalFormat;

public class DecFormatUtil {
    private static final String format3dot2 = "##0.00";
    private static final String format2dot1 = "#0.0";
    private static final String format2dot2 = "#0.00";
    private static final String format2dot3 = "#0.000";
    private static final String format00dot1 = "00.0";
    private static final String format000 = "000";

    public static String format3dot2(float paramFloat) {
        return new DecimalFormat(format3dot2).format(paramFloat);
    }

    public static String format2dot1(float paramFloat) {
        return new DecimalFormat(format2dot1).format(paramFloat);
    }

    public static String format2dot2(float paramFloat) {
        return new DecimalFormat(format2dot2).format(paramFloat);
    }

    public static String format2dot3(float paramFloat) {
        return new DecimalFormat(format2dot3).format(paramFloat);
    }

    public static String format00dot1(float paramFloat) {
        return new DecimalFormat(format00dot1).format(paramFloat);
    }

    public static String format000(int paramInt) {
        return new DecimalFormat(format000).format(paramInt);
    }
}
