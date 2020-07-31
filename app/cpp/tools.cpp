#pragma

#include <jni.h>
#include <string>
#include <termios.h>
#include <stdio.h>
#include <sys/time.h>
#include <unistd.h>
#include <fcntl.h>
#include <vector>
#include "android/log.h"


using namespace std;
static const char *TAG = "obd_core";
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#ifndef _Included_com_miyuan_obd_serial_OBDBusiness
#define _Included_com_miyuan_obd_serial_OBDBusiness
#ifdef __cplusplus
extern "C"
{
#endif

/*
pu8PictureBitmapIn : 图片的bitmap地址指针
pu8PictureOut:图片转换完后的输出指针（4096字节）
返回值：转换完之后的字节长度；
*/
unsigned short g_u16PictureCacheBuf[2048];
unsigned short convert_picture(unsigned char *pu8PictureBitmapIn, unsigned char *pu8PictureOut) {

    int i, j, k;
    unsigned short u16ColorValue, u16ColorNum, u16ColorSameNum, u16PictureOutByteSize = 0;
    unsigned short *pu8TmpOut = (unsigned short *) pu8PictureOut;

    memset(pu8PictureOut, 0, 4096);
    memset(g_u16PictureCacheBuf, 0, 4096);

    for (i = 0, j = 0; i < 3872; i += 2) {
        g_u16PictureCacheBuf[j++] = (pu8PictureBitmapIn[i] << 8) | pu8PictureBitmapIn[i + 1];
    }

    u16ColorValue = g_u16PictureCacheBuf[0];
    u16ColorSameNum = 0;
    for (i = 0; i < 1936; i++) {
        if (u16ColorValue != g_u16PictureCacheBuf[i]) {
            pu8TmpOut[u16PictureOutByteSize++] = u16ColorValue;
            pu8TmpOut[u16PictureOutByteSize++] = u16ColorSameNum;
            u16ColorValue = g_u16PictureCacheBuf[i];
            u16ColorSameNum = 1;
        } else {
            u16ColorSameNum++;
        }
    }

    pu8TmpOut[u16PictureOutByteSize++] = u16ColorValue;
    pu8TmpOut[u16PictureOutByteSize++] = u16ColorSameNum;

    return u16PictureOutByteSize * 2;
}

/*
 * Class:     com_miyuan_obd_HomePage
 * Method:    convertPicture
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_com_miyuan_obd_HomePage_convertPicture(JNIEnv *env, jclass jcl, jbyteArray src,
                                            jbyteArray des) {
    jbyte *srcBuffer = env->GetByteArrayElements(src, 0);
    jbyte *desBuffer = env->GetByteArrayElements(des, 0);
    unsigned short length = convert_picture((unsigned char *) srcBuffer,
                                            (unsigned char *) desBuffer);
    jbyteArray result = env->NewByteArray(length);
    env->SetByteArrayRegion(result, 0, length, desBuffer);
    return result;
}

#ifdef __cplusplus
}
#endif
#endif
