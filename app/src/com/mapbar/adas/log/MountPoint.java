package com.mapbar.adas.log;

/**
 * Created by xiaoyee on 11/16/16.
 */

public class MountPoint {

    public static final String TYPE = "type";

    private boolean mAllowRO;
    private String mDevice;
    private String mPath;
    private boolean mReadWrite;
    private String mType;

    public MountPoint(boolean z, String str, String str2, String str3) {
        this.mReadWrite = z;
        this.mDevice = str;
        this.mPath = str2;
        this.mType = str3;
        boolean z2 = !z || isMountPointROByDefault(str2, str3);
        this.mAllowRO = z2;
    }

    public static MountPoint parse(String str) {
        String[] split = str.split(" ");
        if (split.length < 5) {
            return null;
        }
        int i;
        String str2 = split[0];
        if (split[1].equals("on")) {
            i = 2;
        } else {
            i = 1;
        }
        int i2 = i + 1;
        String str3 = split[i];
        if (split[i2].equals(TYPE)) {
            i = i2 + 1;
        } else {
            i = i2;
        }
        i2 = i + 1;
        String str4 = split[i];
        String str5 = split[i2];
        if (str5.charAt(0) == '(') {
            str5 = str5.substring(1);
        }
        return new MountPoint(str5.startsWith("rw"), str2, str3, str4);
    }

    private static boolean isMountPointROByDefault(String str, String str2) {
        return str.startsWith("/system") || str.startsWith("/mnt/asec/") || str2.equals("rootfs");
    }

    public boolean isFuse() {
        return "fuse".equals(this.mType);
    }

    public String getPath() {
        return this.mPath;
    }

    public String getDevicePath() {
        return this.mDevice;
    }

    public String getType() {
        return this.mType;
    }

    public boolean isMountedRW() {
        return this.mReadWrite;
    }

    public boolean allowsRemount() {
        return this.mAllowRO;
    }

    public boolean isApplicationMountPoint() {
        return this.mPath.startsWith("/mnt/asec");
    }

    public String toString() {
        return String.format("%s, %s, %s, rw: %s", new Object[]{this.mDevice, this.mPath, this.mType, String.valueOf(this.mReadWrite)});
    }
}
