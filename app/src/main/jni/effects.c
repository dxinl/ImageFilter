//
// Created by DengXinliang on 2015/12/7.
//
#include "com_mx_dengxinliang_imageeffects_MainActivity.h"
#include <android/bitmap.h>
#include <android/log.h>

#define LOG_TAG "ImageEffects"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

typedef struct {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t alpha;
} argb;

AndroidBitmapInfo bitmapInfo;
void * bitmapPixels;
int x, y;
int retCode;

int getPositiveNum(int num) {
    if (num < 0) {
        return num * -1;
    }
    return num;
}

JNIEXPORT jboolean JNICALL Java_com_mx_dengxinliang_imageeffects_MainActivity_toGray
(JNIEnv * env, jobject obj, jobject bitmap)
{
    double redWeight = 0.3;
    double greenWeight = 0.59;
    double blueWeight = 0.11;

    if ((retCode = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() is failed. error code = %d", retCode);
        return 0;
    }

    if ((retCode = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() is failed. error code = %d", retCode);
        return 0;
    }

    for (y = 0; y < bitmapInfo.height; ++y) {
        argb * line = (argb *) bitmapPixels;

        for (x = 0; x < bitmapInfo.width; ++x) {
            uint8_t value = (uint8_t) (line[x].red * redWeight + line[x].green * greenWeight + line[x].blue * blueWeight);
            line[x].red = line[x].green = line[x].blue = value;
        }

        bitmapPixels = (char *) bitmapPixels + bitmapInfo.stride;
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return 1;
}

JNIEXPORT jboolean JNICALL Java_com_mx_dengxinliang_imageeffects_MainActivity_toBAW
(JNIEnv * env, jobject obj, jobject bitmap)
{
    if ((retCode = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() is failed. error code = %d", retCode);
        return 0;
    }

    if ((retCode = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() is failed. error code = %d", retCode);
        return 0;
    }

    for (y = 0; y < bitmapInfo.height; ++y) {
        argb * line = (argb *) bitmapPixels;

        for (x = 0; x < bitmapInfo.width; ++x) {
            uint8_t value = (uint8_t) ((line[x].red + line[x].green + line[x].blue) / 3.0);
            if (value >= 100) {
                line[x].red = line[x].green = line[x].blue = 0;
            } else {
                line[x].red = line[x].green = line[x].blue = 255;
            }
        }

        bitmapPixels = (char *) bitmapPixels + bitmapInfo.stride;
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return 1;
}

JNIEXPORT jboolean JNICALL Java_com_mx_dengxinliang_imageeffects_MainActivity_toBackSheet
(JNIEnv * env, jobject obj, jobject bitmap)
{
    if ((retCode = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() is failed. error code = %d", retCode);
        return 0;
    }

    if ((retCode = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() is failed. error code = %d", retCode);
        return 0;
    }

    for (y = 0; y < bitmapInfo.height; ++y) {
        argb * line = (argb *) bitmapPixels;

        for (x = 0; x < bitmapInfo.width; ++x) {
            line[x].red = (uint8_t) 255 - line[x].red;
            line[x].green = (uint8_t) 255 - line[x].green;
            line[x].blue = (uint8_t) 255 - line[x].blue;
        }

        bitmapPixels = (char *) bitmapPixels + bitmapInfo.stride;
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return 1;
}

JNIEXPORT jboolean JNICALL Java_com_mx_dengxinliang_imageeffects_MainActivity_toRelief
(JNIEnv * env, jobject obj, jobject bitmap)
{
    double redWeight = 0.3;
    double greenWeight = 0.59;
    double blueWeight = 0.11;

    if ((retCode = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() is failed. error code = %d", retCode);
        return 0;
    }

    if ((retCode = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() is failed. error code = %d", retCode);
        return 0;
    }

    for (y = 0; y < bitmapInfo.height; ++y) {
        argb * line = (argb *) bitmapPixels;
        for (x = 0; x < bitmapInfo.width - 1; ++x) {
            line[x].red = (uint8_t) getPositiveNum((int) line[x].red - (int) line[x + 1].red + 128);
            line[x].green = (uint8_t) getPositiveNum((int) line[x].green - (int) line[x + 1].green + 128);
            line[x].blue = (uint8_t) getPositiveNum((int) line[x].blue - (int) line[x + 1].blue + 128);

            uint8_t value = (uint8_t) (line[x].red * redWeight + line[x].green * greenWeight + line[x].blue * blueWeight);
            line[x].red = line[x].green = line[x].blue = value;
        }

        bitmapPixels = (char *) bitmapPixels + bitmapInfo.stride;
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return 1;
}