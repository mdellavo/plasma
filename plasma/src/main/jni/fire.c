#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "utils.h"


static uint16_t make565(int red, int green, int blue)
{
	return (uint16_t)(((red << 8) & 0xf800) | ((green << 3) & 0x07e0) | ((blue >> 3) & 0x001f));
}

static  void hsv2rgb( double *r, double *g, double *b, double h, double s, double v )
{
	int i;
	double f, p, q, t;
	if( s == 0 ) {
		// achromatic (grey)
		*r = *g = *b = v;
		return;
	}
	h /= 60;			// sector 0 to 5
	i = floor(h);
	f = h - i;			// factorial part of h
	p = v * ( 1 - s );
	q = v * ( 1 - s * f );
	t = v * ( 1 - s * ( 1 - f ) );
	switch( i ) {
		case 0:
			*r = v;
			*g = t;
			*b = p;
			break;
		case 1:
			*r = q;
			*g = v;
			*b = p;
			break;
		case 2:
			*r = p;
			*g = v;
			*b = t;
			break;
		case 3:
			*r = p;
			*g = q;
			*b = v;
			break;
		case 4:
			*r = t;
			*g = p;
			*b = v;
			break;
		default:		// case 5:
			*r = v;
			*g = p;
			*b = q;
			break;
	}
}


static uint16_t  palette_fire[PALETTE_SIZE];

static void init_palette_fire(void)
{
    int i;
    for (i=0; i<PALETTE_SIZE; i++) {

        double percentile = (double)i  / (double)PALETTE_SIZE;

        double h = percentile / 3.  * 360.;
        double s = 1.;
        double v = min(1. , percentile * 1.25);

        double r,g,b;
        hsv2rgb(&r,&g,&b, h,s,v);

        palette_fire[i] = make565((int)(r * 255), (int)(g * 255), (int)(b * 255));
    }

}

static int32_t **fire;

static void init_fire(int w, int h)
{
    LOGI("init fire palette");

    init_palette_fire();

    if (fire != NULL) {
        int num = sizeof(fire) / sizeof(fire[0]);
        for(int i=0; i<num; i++) {
           free(fire[i]);
        }
        free(fire);
    }

    fire = malloc(w * sizeof(int32_t *));
    for (int x=0; x<w; x++) {
        fire[x] = malloc(h * sizeof(int32_t));
    }

    LOGI("allocate fire 0x%p %dx%d (@ %d bytes)", fire, w, h, sizeof(int32_t));

    for (int x=0; x<w; x++) {
        for (int y=0; y<h; y++) {
            fire[x][y] = 0;
        }
    }

}

static  void seed_fire(int w, int h)
{
   for(int x = 0; x < w; x++) {

       fire[x][h - 1] = abs(rand()) / 30000.;
   }

   for(int y=0; y<h-1; y++) {
       for(int x=0; x<w; x++) {
           fire[x][y] = ((fire[(x - 1 + w) % w][(y + 1) % h]
                             + fire[(x) % w][(y + 1) % h]
                             + fire[(x + 1) % w][(y + 1) % h]
                             + fire[(x) % w][(y + 2) % h])
                             ) / 4.025;

       }
   }
}

static  Fixed color_fire(double t, int w, int h, int x, int y)
{
    return fire[x][y] * 7;
}

JNIEXPORT void JNICALL Java_org_quuux_plasma_FireView_renderFire(JNIEnv *env, jobject obj, jobject bitmap, jlong  time_ms)
{
    AndroidBitmapInfo  info;
    void*              pixels;
    int                ret;
    static Stats       stats;
    static int         init;
    static int         width, height;

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

    if (width != info.width || height != info.height) {
        init = 0;
    }

    width = info.width;
    height = info.height;

    if (!init) {
        init_fire(info.width, info.height);
        stats_init(&stats);
        init = 1;
    }

    stats_startFrame(&stats);

    seed_fire(info.width, info.height);

    /* FIY-AH */
    fill(&info, pixels, time_ms, &color_fire, palette_fire);

    AndroidBitmap_unlockPixels(env, bitmap);

    stats_endFrame(&stats);
}


