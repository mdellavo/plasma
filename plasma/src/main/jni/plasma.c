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

#define  LOG_TAG    "libplasma"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/* Set to 1 to enable debug log traces. */
#define DEBUG 0

/* Set to 1 to optimize memory stores when generating plasma. */
#define OPTIMIZE_WRITES  1

#ifndef max
#define max(x, y) ((x) > (y)) ? (x) : (y)
#endif
#ifndef min
#define min(x, y) ((x) < (y)) ? (x) : (y)
#endif

//srand((unsigned)time(NULL));

/* Return current time in milliseconds */
static double now_ms(void)
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec*1000. + tv.tv_usec/1000.;
}

/* We're going to perform computations for every pixel of the target
 * bitmap. floating-point operations are very slow on ARMv5, and not
 * too bad on ARMv7 with the exception of trigonometric functions.
 *
 * For better performance on all platforms, we're going to use fixed-point
 * arithmetic and all kinds of tricks
 */

typedef int32_t  Fixed;

#define  FIXED_BITS           16
#define  FIXED_ONE            (1 << FIXED_BITS)
#define  FIXED_AVERAGE(x,y)   (((x) + (y)) >> 1)

#define  FIXED_FROM_INT(x)    ((x) << FIXED_BITS)
#define  FIXED_TO_INT(x)      ((x) >> FIXED_BITS)

#define  FIXED_FROM_FLOAT(x)  ((Fixed)((x)*FIXED_ONE))
#define  FIXED_TO_FLOAT(x)    ((x)/(1.*FIXED_ONE))

#define  FIXED_MUL(x,y)       (((int64_t)(x) * (y)) >> FIXED_BITS)
#define  FIXED_DIV(x,y)       (((int64_t)(x) * FIXED_ONE) / (y))

#define  FIXED_DIV2(x)        ((x) >> 1)
#define  FIXED_AVERAGE(x,y)   (((x) + (y)) >> 1)

#define  FIXED_FRAC(x)        ((x) & ((1 << FIXED_BITS)-1))
#define  FIXED_TRUNC(x)       ((x) & ~((1 << FIXED_BITS)-1))

#define  FIXED_FROM_INT_FLOAT(x,f)   (Fixed)((x)*(FIXED_ONE*(f)))

typedef int32_t  Angle;

#define  ANGLE_BITS              9

#if ANGLE_BITS < 8
#  error ANGLE_BITS must be at least 8
#endif

#define  ANGLE_2PI               (1 << ANGLE_BITS)
#define  ANGLE_PI                (1 << (ANGLE_BITS-1))
#define  ANGLE_PI2               (1 << (ANGLE_BITS-2))
#define  ANGLE_PI4               (1 << (ANGLE_BITS-3))

#define  ANGLE_FROM_FLOAT(x)   (Angle)((x)*ANGLE_PI/M_PI)
#define  ANGLE_TO_FLOAT(x)     ((x)*M_PI/ANGLE_PI)

#if ANGLE_BITS <= FIXED_BITS
#  define  ANGLE_FROM_FIXED(x)     (Angle)((x) >> (FIXED_BITS - ANGLE_BITS))
#  define  ANGLE_TO_FIXED(x)       (Fixed)((x) << (FIXED_BITS - ANGLE_BITS))
#else
#  define  ANGLE_FROM_FIXED(x)     (Angle)((x) << (ANGLE_BITS - FIXED_BITS))
#  define  ANGLE_TO_FIXED(x)       (Fixed)((x) >> (ANGLE_BITS - FIXED_BITS))
#endif

static Fixed  angle_sin_tab[ANGLE_2PI+1];

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

/* Color palette used for rendering the plasma */
#define  PALETTE_BITS   8
#define  PALETTE_SIZE   (1 << PALETTE_BITS)

#if PALETTE_BITS > FIXED_BITS
#  error PALETTE_BITS must be smaller than FIXED_BITS
#endif

static uint16_t  palette[PALETTE_SIZE];


static uint16_t make565(int red, int green, int blue)
{
	return (uint16_t)(((red << 8) & 0xf800) | ((green << 3) & 0x07e0) | ((blue >> 3) & 0x001f));
}

static void init_palette_plasma(void)
{
    int  nn, mm = 0;
    /* fun with colors */

    for (nn = 0; nn < PALETTE_SIZE/4; nn++) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette[nn] = make565(255, jj, 255-jj);
    }

    for ( mm = nn; nn < PALETTE_SIZE/2; nn++ ) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette[nn] = make565(255-jj, 255, jj);
    }

    for ( mm = nn; nn < PALETTE_SIZE*3/4; nn++ ) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette[nn] = make565(0, 255-jj, 255);
    }

    for ( mm = nn; nn < PALETTE_SIZE; nn++ ) {
        int  jj = (nn-mm)*4*255/PALETTE_SIZE;
        palette[nn] = make565(jj, 0, 255);
    }
}

static __inline__ uint16_t  palette_from_fixed( Fixed  x )
{
    if (x < 0) x = -x;
    if (x >= FIXED_ONE) x = FIXED_ONE-1;
    int  idx = FIXED_FRAC(x) >> (FIXED_BITS - PALETTE_BITS);
    return palette[idx & (PALETTE_SIZE-1)];
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

//
//    double w = (double)info->width;
//    double h = (double)info->height;
//
//    double color = (
//              128.0 + (128.0 * sin((double)x / 16.0 * t / 10000.0))
//            + 128.0 + (128.0 * sin((double)y / 32.0 / t / 10000.0))
//            - 128.0 + (128.0 * sin(sqrt((double)((x - w / 2.0) * (x - w / 2.0) + (y - h / 2.0) * (y - h / 2.0))) / 8.0 * t / 1000.0))
//            + 128.0 + (128.0 * sin(sqrt((double)(x * x + y * y)) / 8.0 / t / 1000.0))
//         ) / 2.0;
//
//    return FIXED_FROM_FLOAT((double)color / 128.0);

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

        palette[i] = make565((int)(r * 255), (int)(g * 255), (int)(b * 255));
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

static void fill(AndroidBitmapInfo* info, void*  pixels, double  t, Fixed (*color)(double t, int w, int h, int x, int y))
{

    int  yy;
    for (yy = 0; yy < info->height; yy++) {
        uint16_t*  line = (uint16_t*)pixels;
        int xx = 0;

#if OPTIMIZE_WRITES
        /* optimize memory writes by generating one aligned 32-bit store
         * for every pair of pixels.
         */
        uint16_t*  line_end = line + info->width;

        if (line < line_end) {

            if (((uint32_t)line & 3) != 0) {
                Fixed ii = (*color)(t, info->width, info->height, xx, yy);
                xx++;

                line[0] = palette_from_fixed(ii >> 2);
                line++;
            }

            while (line + 2 <= line_end) {
                Fixed i1 = (*color)(t, info->width, info->height, xx, yy);
                xx++;

                Fixed i2 = (*color)(t, info->width, info->height, xx, yy);
                xx++;

                uint32_t  pixel = ((uint32_t)palette_from_fixed(i1 >> 2) << 16) |
                                   (uint32_t)palette_from_fixed(i2 >> 2);

                ((uint32_t*)line)[0] = pixel;
                line += 2;
            }

            if (line < line_end) {
                Fixed ii = (*color)(t, info->width, info->height, xx, yy);
                line[0] = palette_from_fixed(ii >> 2);
                line++;
            }
        }
#else /* !OPTIMIZE_WRITES */
        for (xx = 0; xx < info->width; xx++) {
            Fixed ii = (*color)(t, info->width, info->height, xx, yy);
            line[xx]  = palette_from_fixed(ii / 4);
        }
#endif /* !OPTIMIZE_WRITES */

        // go to next line
        pixels = (char*)pixels + info->stride;
    }
}

/* simple stats management */
typedef struct {
    double  renderTime;
    double  frameTime;
} FrameStats;

#define  MAX_FRAME_STATS  200
#define  MAX_PERIOD_MS    1500

typedef struct {
    double  firstTime;
    double  lastTime;
    double  frameTime;

    int         firstFrame;
    int         numFrames;
    FrameStats  frames[ MAX_FRAME_STATS ];
} Stats;

static void
stats_init( Stats*  s )
{
    s->lastTime = now_ms();
    s->firstTime = 0.;
    s->firstFrame = 0;
    s->numFrames  = 0;
}

static void
stats_startFrame( Stats*  s )
{
    s->frameTime = now_ms();
}

static void
stats_endFrame( Stats*  s )
{
    double now = now_ms();
    double renderTime = now - s->frameTime;
    double frameTime  = now - s->lastTime;
    int nn;

    if (now - s->firstTime >= MAX_PERIOD_MS) {
        if (s->numFrames > 0) {
            double minRender, maxRender, avgRender;
            double minFrame, maxFrame, avgFrame;
            int count;

            nn = s->firstFrame;
            minRender = maxRender = avgRender = s->frames[nn].renderTime;
            minFrame  = maxFrame  = avgFrame  = s->frames[nn].frameTime;
            for (count = s->numFrames; count > 0; count-- ) {
                nn += 1;
                if (nn >= MAX_FRAME_STATS)
                    nn -= MAX_FRAME_STATS;
                double render = s->frames[nn].renderTime;
                if (render < minRender) minRender = render;
                if (render > maxRender) maxRender = render;
                double frame = s->frames[nn].frameTime;
                if (frame < minFrame) minFrame = frame;
                if (frame > maxFrame) maxFrame = frame;
                avgRender += render;
                avgFrame  += frame;
            }
            avgRender /= s->numFrames;
            avgFrame  /= s->numFrames;

            LOGI("frame/s (avg,min,max) = (%.1f,%.1f,%.1f) "
                 "render time ms (avg,min,max) = (%.1f,%.1f,%.1f)\n",
                 1000./avgFrame, 1000./maxFrame, 1000./minFrame,
                 avgRender, minRender, maxRender);
        }
        s->numFrames  = 0;
        s->firstFrame = 0;
        s->firstTime  = now;
    }

    nn = s->firstFrame + s->numFrames;
    if (nn >= MAX_FRAME_STATS)
        nn -= MAX_FRAME_STATS;

    s->frames[nn].renderTime = renderTime;
    s->frames[nn].frameTime  = frameTime;

    if (s->numFrames < MAX_FRAME_STATS) {
        s->numFrames += 1;
    } else {
        s->firstFrame += 1;
        if (s->firstFrame >= MAX_FRAME_STATS)
            s->firstFrame -= MAX_FRAME_STATS;
    }

    s->lastTime = now;
}

// FIXME export symbols that wrap an inner tick/render func


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
    fill(&info, pixels, time_ms, &color_plasma);

    AndroidBitmap_unlockPixels(env, bitmap);

    stats_endFrame(&stats);
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
    fill(&info, pixels, time_ms, &color_fire);

    AndroidBitmap_unlockPixels(env, bitmap);

    stats_endFrame(&stats);
}

#define BLUR_LENGTH 3

typedef struct  {
    double x, y, z;
    double dz;
    double screen_x[BLUR_LENGTH], screen_y[BLUR_LENGTH];
} Star;

typedef struct {
    double rot_x, rot_y, rot_z;
    double rot_dx, rot_dy, rot_dz;
    double rot_ax, rot_ay, rot_az;
} Camera;

static Camera camera;

#define NUM_STARS 10000

static Star stars[NUM_STARS];

#define CAMERA_STEP .0001
#define DAMPING .8

#define STAR_MIN_X -5000.
#define STAR_MAX_X 5000.

#define STAR_MIN_Y -5000.
#define STAR_MAX_Y 5000.

#define STAR_MIN_Z 100.
#define STAR_MAX_Z 1000.

#define STAR_MIN_DZ .5
#define STAR_MAX_DZ 2.

#define PI 3.1415926535897932384626433832795

#define radians(angle) ((angle * PI) / 180.)

static __inline__ double randrange(double min, double max) {
    double p = ((double)rand()/(double)RAND_MAX);
    return min + (p * (max - min));
}

static void init_starfield(int width, int height) {
    camera.rot_x = camera.rot_y = camera.rot_z = 0;
    camera.rot_dx = camera.rot_dy = camera.rot_dz = 0;
    camera.rot_ax = camera.rot_ay = camera.rot_az = 0;

    for(int i=0; i<NUM_STARS; i++) {
        stars[i].x = randrange(STAR_MIN_X, STAR_MAX_X);
        stars[i].y = randrange(STAR_MIN_Y, STAR_MAX_Y);
        stars[i].z = randrange(STAR_MIN_Z, STAR_MAX_Z);
        stars[i].dz = randrange(STAR_MIN_DZ, STAR_MAX_DZ);
    }
}

static __inline__ void set_pixel(AndroidBitmapInfo *info, void *pixels, int x, int y, uint16_t color) {
    pixels = (char *)pixels + (info->stride * y);
    uint16_t *line = (uint16_t*)pixels;
    line[x] = color;
}

static __inline__ void inc_pixel(AndroidBitmapInfo *info, void *pixels, int x, int y, uint16_t color) {
    pixels = (char *)pixels + (info->stride * y);
    uint16_t *line = (uint16_t*)pixels;
    line[x] += color;
}

static __inline__ void set_wupixel(AndroidBitmapInfo *info, void *pixels, double x, double y, int bri) {
    set_pixel(info, pixels, x, y, make565(bri, bri, bri));
    set_pixel(info, pixels, x+1, y, make565(bri, bri, bri));
    set_pixel(info, pixels, x, y+1, make565(bri, bri, bri));
    set_pixel(info, pixels, x+1, y+1, make565(bri, bri, bri));
}

static __inline__ void inc_wupixel(AndroidBitmapInfo *info, void *pixels, double x, double y, int bri) {

    double fx = x - floor(x);
    double fy = y - floor(y);

    int btl = (int)round((1.-fx) * (1.-fy) * (double)bri);
    int btr = (int)round((fx)  * (1.-fy) * (double)bri);
    int bbl = (int)round((1.-fx) *  (fy)  * (double)bri);
    int bbr = (int)round((fx)  *  (fy)  * (double)bri);

    inc_pixel(info, pixels, x, y, make565(btl, btl, btl));
    inc_pixel(info, pixels, x+1, y, make565(btr, btr, btr));
    inc_pixel(info, pixels, x, y+1, make565(bbl, bbl, bbl));
    inc_pixel(info, pixels, x+1, y+1, make565(bbr, bbr, bbr));

}

static __inline__ double rotate_x(double *x, double *y, double *z, double angle) {
   double rad = radians(angle);
   double cosa = cos(rad);
   double sina = sin(rad);
   *y = *y * cosa - *z * sina;
   *z = *y * sina + *z * cosa;
}

static __inline__ double rotate_y(double *x, double *y, double *z, double angle) {
   double rad = radians(angle);
   double cosa = cos(rad);
   double sina = sin(rad);
   *z = *z * cosa - *x * sina;
   *x = *z * sina + *x * cosa;
}

static __inline__ double rotate_z(double *x, double *y, double *z, double angle) {
   double rad = radians(angle);
   double cosa = cos(rad);
   double sina = sin(rad);
   *x = *x * cosa - *y * sina;
   *y = *x * sina + *y * cosa;
}


JNIEXPORT void JNICALL Java_org_quuux_plasma_StarFieldView_renderStarField(JNIEnv *env, jobject obj, jobject bitmap, jlong  time_ms)
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
        init_starfield(info.width, info.height);
        stats_init(&stats);
        init = 1;
    }

    stats_startFrame(&stats);

    camera.rot_ax += randrange(-CAMERA_STEP, CAMERA_STEP);
    camera.rot_ay += randrange(-CAMERA_STEP, CAMERA_STEP);
    camera.rot_az += randrange(-CAMERA_STEP, CAMERA_STEP);

    camera.rot_dx += camera.rot_ax;
    camera.rot_dy += camera.rot_ay;
    camera.rot_dz += camera.rot_az;

    camera.rot_ax *= DAMPING;
    camera.rot_ay *= DAMPING;
    camera.rot_az *= DAMPING;

    camera.rot_dx *= DAMPING;
    camera.rot_dy *= DAMPING;
    camera.rot_dz *= DAMPING;

    camera.rot_x = (camera.rot_x + camera.rot_dx) * DAMPING;
    camera.rot_y = (camera.rot_y + camera.rot_dy) * DAMPING;
    camera.rot_z = (camera.rot_z + camera.rot_dz) * DAMPING;

    /* final frontier */

    for (int i=0; i<NUM_STARS; i++) {

        Star *star = &stars[i];

        for (int j=0; j<BLUR_LENGTH; j++) {
            if (star->screen_x[j] >= 0 && star->screen_y[j] >= 0)
                set_wupixel(&info, pixels, star->screen_x[j], star->screen_y[j], 0);
        }

        for (int j=BLUR_LENGTH-2; j>=0; j--) {
            star->screen_x[j+1] = star->screen_x[j];
            star->screen_y[j+1] = star->screen_y[j];
        }

        // move
        star->z -= star->dz;

        // rotate
        double tmp_x = star->x;
        double tmp_y = star->y;
        double tmp_z = star->z;

        rotate_x(&tmp_x, &tmp_y, &tmp_z, camera.rot_x);
        rotate_y(&tmp_x, &tmp_y, &tmp_z, camera.rot_y);
        rotate_z(&tmp_x, &tmp_y, &tmp_z, camera.rot_z);

        // project
        star->screen_x[0] = (tmp_x / tmp_z * 100.) + ((double)width / 2.);
        star->screen_y[0] = (tmp_y / tmp_z * 100.) + ((double)height / 2.);

        int on_screen = star->screen_x[0] >= 0 && star->screen_x[0] < width && star->screen_y[0] >= 0 && star->screen_y[0] < height;

        if (on_screen) {

            int dx = star->screen_x[1] - star->screen_x[0];
            int dy = star->screen_y[1] - star->screen_y[0];
            double length = sqrt(dx*dx + dy*dy);

            //double bri = ((255./5.) * star->dz) * (1000. / star->z);
            double bri = (50000.*star->dz) / star->z;
            if (length > 1.)
                bri /= length;

            for (int j=0; j<BLUR_LENGTH; j++)
                if (star->screen_x[j] >= 0 && star->screen_y[j] >= 0)
                    inc_wupixel(&info, pixels, star->screen_x[j], star->screen_y[j], bri);
        }

        if (!on_screen || star->z < 0.) {
            star->x = randrange(STAR_MIN_X, STAR_MAX_X);
            star->y = randrange(STAR_MIN_Y, STAR_MAX_Y);
            star->z = randrange(STAR_MIN_Z, STAR_MAX_Z);
            star->dz = randrange(STAR_MIN_DZ, STAR_MAX_DZ);

            for (int j=0; j<BLUR_LENGTH; j++) {
                star->screen_x[j] = -1;
                star->screen_y[j] = -1;
            }
        }

    }

    AndroidBitmap_unlockPixels(env, bitmap);

    stats_endFrame(&stats);
}
