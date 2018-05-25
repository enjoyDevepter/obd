package com.mapbar.adas.log;

/**
 * Created by xiaoyee on 11/16/16.
 */

public class MathUtil {
    public static int getPercent(int i, int i2) {
        return (int) ((((float) i) * 100.0f) / ((float) i2));
    }

    public static int getPercent(long j, long j2) {
        if (j2 == 0) {
            return 0;
        }
        return (int) ((((float) j) * 100.0f) / ((float) j2));
    }

    public static float getPercentFloat(long j, long j2) {
        if (j2 == 0) {
            return 0.0f;
        }
        return (((float) j) * 1.0f) / ((float) j2);
    }
}
