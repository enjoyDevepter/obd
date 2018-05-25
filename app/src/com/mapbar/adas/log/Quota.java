package com.mapbar.adas.log;

import android.os.Build;
import android.os.StatFs;

/**
 * Created by xiaoyee on 11/16/16
 * 存储设备配额
 */

public class Quota {

    private boolean isValid;
    private long totalSpace;
    private long usedSpace;


    public Quota(long j, long j2) {
        this.isValid = true;
        this.totalSpace = j;
        this.usedSpace = j2;
    }

    public static Quota getQuotaForPath(String str) {
        try {
            long blockSize;
            long availableBlocks;
            long blockCount;
            if ("/".equals(str)) {
                str = "/system";
            }
            StatFs statFs = new StatFs(str);
            if (Build.VERSION.SDK_INT < 18) {
                blockSize = (long) statFs.getBlockSize();
                availableBlocks = (long) statFs.getAvailableBlocks();
                blockCount = (long) statFs.getBlockCount();
            } else {
                blockSize = statFs.getBlockSizeLong();
                availableBlocks = statFs.getAvailableBlocksLong();
                blockCount = statFs.getBlockCountLong();
            }
            long j = blockSize * blockCount;
            return new Quota(j, j - (blockSize * availableBlocks));
        } catch (Throwable e) {
            return new Quota(0, 0);
        }
    }

    public void invalidate() {
        this.isValid = false;
    }

    public boolean isValid() {
        return this.isValid;
    }

    private Quota merge(Quota quota) {
        this.totalSpace += quota.getTotalSpace();
        this.usedSpace += quota.getUsedSpace();
        return this;
    }

    public long getTotalSpace() {
        return this.totalSpace;
    }

    public Quota setTotalSpace(long j) {
        this.totalSpace = j;
        return this;
    }

    public long getUsedSpace() {
        return this.usedSpace;
    }

    public Quota setUsedSpace(long j) {
        this.usedSpace = j;
        return this;
    }

    public long getFreeSpace() {
        return this.totalSpace - this.usedSpace;
    }

    public int getPercentUsed() {
        if (this.totalSpace == Long.MAX_VALUE) {
            return 0;
        }
        int percent = MathUtil.getPercent(this.usedSpace, this.totalSpace);
        if (percent != 0 || this.totalSpace <= 0) {
            return percent;
        }
        return 1;
    }

    public boolean isUnlimited() {
        return this.totalSpace == Long.MAX_VALUE;
    }

    public boolean isEmpty() {
        return this.totalSpace == 0;
    }
}
