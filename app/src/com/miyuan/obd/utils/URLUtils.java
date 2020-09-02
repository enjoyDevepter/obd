package com.miyuan.obd.utils;

/**
 * Created by guomin on 2018/6/3.
 */

public class URLUtils {
    //    public static final String HOST = "http://box.iccm.cn/";
//    public static final String HOST = "http://box.1668288.com/";
    public static final String HOST = "http://tpms.1668288.com/";
    public static final String ACTIVATE = HOST + "service/lisense/activation";
    public static final String ACTIVATE_SUCCESS = HOST + "service/lisense/activationResult";
    public static final String GET_TIME = HOST + "service/tool/getServerTime";
    public static final String GET_SMS = HOST + "service/sms/send";
    public static final String GET_CAR_BRAND = HOST + "service/car/getCars";
    public static final String MODIFY_CAR_BRAND = HOST + "service/car/updateCar";
    public static final String GET_LISENSE = HOST + "service/lisense/getLisense";
    public static final String FIRMWARE_UPDATE = HOST + "service/update/getUpdate";
    public static final String FIRMWARE_UPDATE_SUCCESS = HOST + "service/update/updateResult";
    public static final String APK_UPDATE = HOST + "service/soft/getUpdate";
    public static final String SMS_CHECK = HOST + "service/sms/check";
    public static final String SN_CHECK = HOST + "service/box/check";
    public static final String TIRE_CHECK = HOST + "service/box/checkTPMS";
    public static final String CLEAR_PARAM = HOST + "service/paramTemp/addParamTemp";
    public static final String GET_USER_INFO = HOST + "service/box/getUserInfo";
    public static final String RIGHT_CHECK = HOST + "service/box/getSNRight";
    public static final String UPDATE_TIRE = HOST + "service/data/uploadData";
    public static final String UPDATE_ERROR_FILE = HOST + "service/data/uploadFile";
    public static final String UPDATE_STATUS = HOST + "service/data/check";
    public static final String UPDATE_SEN_STATUS = HOST + "service/box/updateBlmd";
    public static final String UPDATE_STATE_PARAMS = HOST + "service/data/uploadBoxParams";
    public static final String UPDATE_FIRMWARE = HOST + "service/update/getUpdate";
    public static final String UPDATE_WARM_PARAMS = UPDATE_STATE_PARAMS;
}
