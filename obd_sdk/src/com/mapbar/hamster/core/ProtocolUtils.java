package com.mapbar.hamster.core;

/**
 * Created by guomin on 2018/6/2.
 */

public class ProtocolUtils {

    public static final int PROTOCOL_HEAD_TAIL = 0x7e;
    private static final int PROTOCAL_COMMON_00 = 0x80;
    private static final int PROTOCAL_COMMON_01 = 0x81;
    private static final int PROTOCAL_COMMON_02 = 0x82;
    private static final int PROTOCAL_COMMON_03 = 0x83;
    private static final int PROTOCAL_COMMON_05 = 0x85;
    private static final int PROTOCAL_COMMON_06 = 0x86;

    public static byte[] verify(long time) {
        byte[] result = new byte[13];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_00;
        result[2] = 01;
        byte[] bytes = HexUtils.longToByte(time);
        result[3] = bytes[0];
        int cr = result[1] ^ result[2] ^ result[3];
        for (int i = 1; i < bytes.length; i++) {
            result[3 + i] = bytes[i];
            cr = cr ^ bytes[i];
        }
        result[11] = (byte) cr;
        result[12] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] auth(String sn, String authCode) {
        byte[] snBytes = sn.getBytes();
        byte[] authBytes = authCode.getBytes();
        byte[] result = new byte[snBytes.length + authBytes.length + 5];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_00;
        result[2] = 02;
        int cr = result[1] ^ result[2];
        for (int i = 0; i < snBytes.length; i++) {
            result[3 + i] = snBytes[i];
            cr = cr ^ snBytes[i];
        }
        for (int i = 0; i < authBytes.length; i++) {
            result[snBytes.length + 3 + i] = authBytes[i];
            cr = cr ^ authBytes[i];
        }
        result[result.length - 2] = (byte) cr;
        result[result.length - 1] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] getVersion() {
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_01;
        result[2] = 01;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] study() {
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_02;
        result[2] = 01;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] getTirePressureStatus() {
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_02;
        result[2] = 03;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] setSensitive(int sensitive) {
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_02;
        result[2] = 04;
        result[3] = (byte) sensitive;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] playWarm(int warmType) {
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_03;
        result[2] = 01;
        result[3] = (byte) warmType;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] test(byte[] date) {
        byte[] result = new byte[date.length + 5];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 88;
        result[2] = 01;
        int cr = result[1] ^ result[2];
        for (int i = 0; i < date.length; i++) {
            result[3 + i] = date[i];
            cr = cr ^ date[i];
        }
        result[result.length - 2] = (byte) cr;
        result[result.length - 1] = PROTOCOL_HEAD_TAIL;
        return result;
    }
}

