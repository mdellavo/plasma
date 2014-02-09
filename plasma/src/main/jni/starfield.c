#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "utils.h"


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

#define CAMERA_STEP .001
#define DAMPING .8

#define STAR_MIN_X -5000.
#define STAR_MAX_X 5000.

#define STAR_MIN_Y -5000.
#define STAR_MAX_Y 5000.

#define STAR_MIN_Z 100.
#define STAR_MAX_Z 1000.

#define STAR_MIN_DZ .5
#define STAR_MAX_DZ 2.

static uint16_t make565(int red, int green, int blue)
{
	return (uint16_t)(((red << 8) & 0xf800) | ((green << 3) & 0x07e0) | ((blue >> 3) & 0x001f));
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
