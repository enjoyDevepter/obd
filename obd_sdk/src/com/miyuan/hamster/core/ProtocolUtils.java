package com.miyuan.hamster.core;

import com.miyuan.hamster.log.Log;

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
    private static final int PROTOCAL_COMMON_08 = 0x88;
    private static final int PROTOCAL_COMMON_09 = 0x90;

    public static byte[] getOBDStatus(long time) {
        Log.d("Protocol getOBDStatus ==");
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
        Log.d("Protocol auth ===");
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

    /**
     * 获取终端版本
     *
     * @return
     */
    public static byte[] checkMatchingStatus() {
        Log.d("Protocol checkMatchingStatus ===");
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
        Log.d("Protocol study ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_02;
        result[2] = 01;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] getStudyProgess() {
        Log.d("Protocol getStudyProgess ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_02;
        result[2] = 02;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] getNewTirePressureStatus() {
        Log.d("Protocol getNewTirePressureStatus ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_08;
        result[2] = 03;
        result[3] = 2;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }


    public static byte[] stopGetNewTirePressureStatus() {
        Log.d("Protocol stopGetNewTirePressureStatus ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_08;
        result[2] = 03;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] tirePressureStatusUpdateSucess() {
        Log.d("Protocol getNewTirePressureStatus ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_08;
        result[2] = 03;
        result[3] = (byte) 0xFF;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 怠速
     *
     * @return
     */
    public static byte[] idling() {
        Log.d("Protocol idling ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x89;
        result[2] = 01;
        result[3] = 1;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 开始行驶
     *
     * @return
     */
    public static byte[] run() {
        Log.d("Protocol run ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x89;
        result[2] = 01;
        result[3] = 2;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 开始采集
     *
     * @return
     */
    public static byte[] startCollect() {
        Log.d("Protocol startCollect ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x89;
        result[2] = 01;
        result[3] = 03;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 停止采集
     *
     * @return
     */
    public static byte[] stopCollect() {
        Log.d("Protocol stopCollect ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x89;
        result[2] = 01;
        result[3] = 00;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 心跳包
     *
     * @return
     */
    public static byte[] sentHeart() {
        Log.d("Protocol sentHeart ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = 62;
        result[2] = 00;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] sendPhysical(int step) {
        Log.d("Protocol sendPhysical === " + step);
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 136;
        result[2] = (byte) 05;
        result[3] = 01;
        result[4] = 01;
        result[5] = (byte) step;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] getFaultCode() {
        Log.d("Protocol getFaultCode ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_08;
        result[2] = 01;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] clearFaultCode() {
        Log.d("Protocol clearFaultCode ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_08;
        result[2] = 02;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] setSensitive(int sensitive) {
        Log.d("Protocol setSensitive ===");
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
        Log.d("Protocol playWarm ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_03;
        result[2] = 01;
        result[3] = (byte) warmType;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] updateInfo(byte[] version, byte[] packageSize) {
        Log.d("Protocol updateInfo ===");
        byte[] result = new byte[version.length + packageSize.length + 5];
        byte[] temp = new byte[version.length + packageSize.length];
        System.arraycopy(version, 0, temp, 0, version.length);
        System.arraycopy(packageSize, 0, temp, version.length, packageSize.length);
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_06;
        result[2] = 01;
        int cr = result[1] ^ result[2];
        for (int i = 0; i < temp.length; i++) {
            result[3 + i] = temp[i];
            cr = cr ^ temp[i];
        }
        result[result.length - 2] = (byte) cr;
        result[result.length - 1] = PROTOCOL_HEAD_TAIL;
        return result;
    }


    public static byte[] updateForUnit(int index, byte[] date) {
        Log.d("Protocol updateForUnit ===");
        byte[] result = new byte[date.length + 6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_06;
        result[2] = 02;
        result[3] = (byte) index;
        int cr = result[1] ^ result[2] ^ result[3];
        for (int i = 0; i < date.length; i++) {
            result[4 + i] = date[i];
            cr = cr ^ date[i];
        }
        result[result.length - 2] = (byte) cr;
        result[result.length - 1] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 参数更新
     *
     * @param sn
     * @param params
     * @return
     */
    public static byte[] updateParams(String sn, String params) {
        Log.d("Protocol updateParams ===");
        byte[] snBytes = sn.getBytes();
        byte[] authBytes = params.getBytes();
        byte[] result = new byte[snBytes.length + authBytes.length + 5];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_05;
        result[2] = 01;
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

    public static byte[] reset() {
        Log.d("Protocol reset ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x86;
        result[2] = 17;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] setCarID() {
        Log.d("Protocol setCarID ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_05;
        result[2] = 03;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 清除参数
     *
     * @return
     */
    public static byte[] cleanParams() {
        Log.d("Protocol cleanParams ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x85;
        result[2] = 02;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }


    /**
     * 误报
     *
     * @return
     */
    public static byte[] misinformation() {
        Log.d("Protocol misinformation ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x82;
        result[2] = 05;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 漏报
     *
     * @return
     */
    public static byte[] disclose() {
        Log.d("Protocol disclose ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x82;
        result[2] = 05;
        result[3] = 1;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }


    /**
     * 漏报
     *
     * @return
     */
    public static byte[] resetSens() {
        Log.d("Protocol resetSens ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x82;
        result[2] = 05;
        result[3] = 2;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 获取HUD区域属性
     *
     * @return
     */
    public static byte[] getHUDStatus() {
        Log.d("Protocol getHUDStatus ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8A;
        result[2] = 02;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 设置HUD区域属性
     *
     * @return
     */
    public static byte[] setHUDStatus(int type, int value) {
        Log.d("Protocol setHUDStatus ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8A;
        result[2] = 02;
        result[3] = 1;
        result[4] = (byte) type;
        result[5] = (byte) value;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 获取HUD预警属性
     *
     * @return
     */
    public static byte[] getHUDWarmStatus() {
        Log.d("Protocol getHUDWarmStatus ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8B;
        result[2] = 02;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 设置HUD预警属性
     *
     * @return
     */
    public static byte[] setHUDWarmStatus(int type, int value) {
        Log.d("Protocol setHUDWarmStatus ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8B;
        result[2] = 02;
        result[3] = 1;
        result[4] = (byte) type;
        result[5] = (byte) value;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 获取盒子参数
     *
     * @return
     */
    public static byte[] getHUDParams() {
        Log.d("Protocol getHUDParams ===");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8B;
        result[2] = 01;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 设置FM参数
     *
     * @return
     */
    public static byte[] setFMParams(int value) {
        Log.d("Protocol setFMParams ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8C;
        result[2] = 01;
        result[3] = 01;
        result[4] = (byte) ((value >> 8) & 0xFF);
        result[5] = (byte) (value & 0xFF);
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 查询FM参数
     *
     * @return
     */
    public static byte[] getFMParams() {
        Log.d("Protocol setFMParams ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8C;
        result[2] = 01;
        result[3] = 02;
        result[4] = 0x00;
        result[5] = 0x00;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] setFMParams(boolean open) {
        Log.d("Protocol setFMParams ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8C;
        result[2] = 01;
        result[3] = (byte) (open ? 01 : 00);
        result[4] = 00;
        result[5] = 00;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 设置HUD参数
     *
     * @return
     */
    public static byte[] setHUDParams(int type, int value) {
        Log.d("Protocol setHUDParams ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x8B;
        result[2] = 01;
        result[3] = 1;
        result[4] = (byte) type;
        result[5] = (byte) value;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 转向
     *
     * @param turnType
     * @return
     */
    public static byte[] getTurnInfo(int turnType, int distance) {
        Log.d("Protocol getTurnInfo ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x90;
        result[2] = 01;
        result[3] = (byte) turnType;
        result[4] = (byte) ((distance >> 8) & 0xFF); // 转向距离
        result[5] = (byte) (distance & 0xFF);
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 转向
     *
     * @param turnType
     * @return
     */
    public static byte[] getTurnInfo2(int turnType, int distance, int retainDistance, int retainTime, byte[] nextRoadName, byte[] exitInfo) {
        Log.d("Protocol getTurnInfo2 ===");
        int roadLength = null != nextRoadName ? nextRoadName.length : 0;
        int exitInfoLength = null != exitInfo ? exitInfo.length : 0;
        byte[] result = new byte[20 + roadLength + exitInfoLength];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x90;
        result[2] = 01;
        // 转向类型
        result[3] = (byte) turnType;
        // 转向距离
        result[4] = (byte) ((distance >> 8) & 0xFF);
        result[5] = (byte) (distance & 0xFF);
        // 剩余距离，单位米
        result[6] = (byte) ((retainDistance >> 24) & 0xFF);
        result[7] = (byte) ((retainDistance >> 16) & 0xFF);
        result[8] = (byte) ((retainDistance >> 8) & 0xFF);
        result[9] = (byte) (retainDistance & 0xFF);
        // 剩余时间 秒
        result[10] = (byte) ((retainTime >> 24) & 0xFF);
        result[11] = (byte) ((retainTime >> 16) & 0xFF);
        result[12] = (byte) ((retainTime >> 8) & 0xFF);
        result[13] = (byte) (retainTime & 0xFF);
        // 当前道路名长
        result[14] = (byte) ((roadLength >> 8) & 0xFF);
        result[15] = (byte) (roadLength & 0xFF);
        // 当前道路名
        if (roadLength > 0) {
            System.arraycopy(nextRoadName, 0, result, 16, roadLength);
        }
        // 出口信息长度
        result[16 + roadLength] = (byte) ((exitInfoLength >> 8) & 0xFF);
        result[17 + roadLength] = (byte) (exitInfoLength & 0xFF);
        if (exitInfoLength > 0) {
            System.arraycopy(exitInfo, 0, result, 18 + roadLength, exitInfoLength);
        }
        byte check = 0;
        for (int i = 1; i < 18 + roadLength + exitInfoLength; i++) {
            check ^= result[i];
        }
        result[18 + roadLength + exitInfoLength] = check;
        result[19 + roadLength + exitInfoLength] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 转向心跳
     *
     * @return
     */
    public static byte[] getTurnInfo() {
        Log.d("Protocol getTurnInfo update===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x90;
        result[2] = 01;
        result[3] = (byte) 0xFE;
        result[4] = (byte) 0x00;
        result[5] = (byte) 0x00;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 车道线
     *
     * @return
     */
    public static byte[] getLineInfo(boolean show, int count, int enter) {
        Log.d("Protocol getLineInfo ===");
        byte[] result = new byte[8];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x90;
        result[2] = 02;
        result[3] = (byte) (show ? 1 : 0);
        result[4] = (byte) count;
        result[5] = (byte) enter;
        result[6] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5]);
        result[7] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    /**
     * 车道线
     *
     * @return
     */
    public static byte[] getLineInfo(boolean show, int count, int enter, byte[] laneType) {
        Log.d("Protocol getLineInfo ===");
        byte[] result = new byte[8 + count];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x90;
        result[2] = 02;
        result[3] = (byte) (show ? 1 : 0);
        result[4] = (byte) count;
        result[5] = (byte) enter;
        if (count > 0) {
            System.arraycopy(laneType, 0, result, 6, count);
        }
        byte check = 0;
        for (int i = 1; i < 6 + count; i++) {
            check ^= result[i];
        }
        result[6 + count] = (byte) check;
        result[7 + count] = PROTOCOL_HEAD_TAIL;
        return result;
    }


    /**
     * 电子狗1
     *
     * @param type
     * @return
     */
    public static byte[] getCameraInfo(boolean show, int type, int speed, int averageSpeed, int distance) {
        Log.d("Protocol getCameraInfo ===");
        byte[] result = new byte[11];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x90;
        result[2] = 03;
        result[3] = (byte) (show ? 1 : 0);
        result[4] = (byte) type;
        result[5] = (byte) (speed & 0xFF);
        result[6] = (byte) (averageSpeed & 0xFF);
        result[7] = (byte) ((distance >> 8) & 0xFF);
        result[8] = (byte) (distance & 0xFF);
        result[9] = (byte) (result[1] ^ result[2] ^ result[3] ^ result[4] ^ result[5] ^ result[6] ^ result[7] ^ result[8]);
        result[10] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] getImage(byte[] buf) {
        Log.d("Protocol getImage ===");
        byte[] result = new byte[6 + buf.length];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_09;
        result[2] = 06;
        result[3] = (byte) ((byte) (buf.length >> 8) & 0xFF);
        result[4] = (byte) (buf.length & 0xFF);
        System.arraycopy(buf, 0, result, 5, buf.length);
        result[5 + buf.length] = PROTOCOL_HEAD_TAIL;
        return result;
    }
}

