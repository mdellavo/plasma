/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "utils.h"



static Fixed  angle_sin_tab[ANGLE_2PI+1];

static uint16_t  make565(int red, int green, int blue)
{
    return (uint16_t)( ((red   << 8) & 0xf800) |
                       ((green << 2) & 0x03e0) |
                       ((blue  >> 3) & 0x001f) );
}

static void init_angles(void)
{
    int  nn;
    for (nn = 0; nn < ANGLE_2PI+1; nn++) {
        double  radians = nn*M_PI/ANGLE_PI;
        angle_sin_tab[nn] = FIXED_FROM_FLOAT(sin(radians));
    }
}

static __inline__ Fixed angle_sin( Angle  a )
{
    return angle_sin_tab[(uint32_t)a & (ANGLE_2PI-1)];
}

static __inline__ Fixed angle_cos( Angle  a )
{
    return angle_sin(a + ANGLE_PI2);
}

static __inline__ Fixed fixed_sin( Fixed  f )
{
    return angle_sin(ANGLE_FROM_FIXED(f));
}

static __inline__ Fixed  fixed_cos( Fixed  f )
{
    return angle_cos(ANGLE_FROM_FIXED(f));
}

static uint16_t  palette_plasma[PALETTE_SIZE];



static void init_palette_plasma(void)
{
    int  nn, mm = 0;
    /* fun with colors */

    for (nn = 0; nn < PALETTE_SIZE/4; nn++) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette_plasma[nn] = make565(255, jj, 255-jj);
    }

    for ( mm = nn; nn < PALETTE_SIZE/2; nn++ ) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette_plasma[nn] = make565(255-jj, 255, jj);
    }

    for ( mm = nn; nn < PALETTE_SIZE*3/4; nn++ ) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette_plasma[nn] = make565(0, 255-jj, 255);
    }

    for ( mm = nn; nn < PALETTE_SIZE; nn++ ) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette_plasma[nn] = make565(jj, 0, 255);
    }
}


/* Angles expressed as fixed point radians */

static void init_plasma(int w, int h)
{
    init_palette_plasma();
    init_angles();
}

static __inline__ Fixed color_plasma(double t, int w, int h, int x, int y)
{

#define  XT1_INCR  FIXED_FROM_FLOAT(1/173./2.)
#define  XT2_INCR  FIXED_FROM_FLOAT(1/242./2.)

#define  YT1_INCR   FIXED_FROM_FLOAT(1/100./2.)
#define  YT2_INCR   FIXED_FROM_FLOAT(1/163./2.)


     Fixed xt1 = FIXED_FROM_FLOAT(t/30000.);
     Fixed xt2 = xt1;

     xt1 += (XT1_INCR * x);
     xt2 -= (XT2_INCR * x);

     Fixed yt1 = FIXED_FROM_FLOAT(t/12300.);
     Fixed yt2 = yt1;

     yt1 -= (YT1_INCR * y);
     yt2 += (YT2_INCR * y);

     return fixed_sin(yt1) + fixed_sin(yt2) + fixed_sin(xt1) + fixed_sin(xt2);
}


JNIEXPORT void JNICALL Java_org_quuux_plasma_PlasmaView_renderPlasma(JNIEnv *env, jobject  obj, jobject bitmap,  jlong  time_ms)
{
    AndroidBitmapInfo  info;
    void*              pixels;
    int                ret;
    static Stats       stats;
    static int         init;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("Bitmap format is not RGB_565 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    if (!init) {
        init_plasma(info.width, info.height);
        stats_init(&stats);
        init = 1;
    }

    stats_startFrame(&stats);

    /* Now fill the values with a nice little plasma */
    fill(&info, pixels, time_ms, &color_plasma, palette_plasma);

    AndroidBitmap_unlockPixels(env, bitmap);

    stats_endFrame(&stats);
}

