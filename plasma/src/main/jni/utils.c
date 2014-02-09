#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "utils.h"

__inline__ double randrange(double min, double max) {
    double p = ((double)rand()/(double)RAND_MAX);
    return min + (p * (max - min));
}


__inline__ uint16_t  palette_from_fixed(uint16_t *palette, Fixed  x)
{
    if (x < 0) x = -x;
    if (x >= FIXED_ONE) x = FIXED_ONE-1;
    int  idx = FIXED_FRAC(x) >> (FIXED_BITS - PALETTE_BITS);
    return palette[idx & (PALETTE_SIZE-1)];
}

/* Return current time in milliseconds */
 double now_ms(void)
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec*1000. + tv.tv_usec/1000.;
}

void fill(AndroidBitmapInfo* info, void*  pixels, double  t, Fixed (*color)(double t, int w, int h, int x, int y), uint16_t *palette)
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

                line[0] = palette_from_fixed(palette, ii >> 2);
                line++;
            }

            while (line + 2 <= line_end) {
                Fixed i1 = (*color)(t, info->width, info->height, xx, yy);
                xx++;

                Fixed i2 = (*color)(t, info->width, info->height, xx, yy);
                xx++;

                uint32_t  pixel = ((uint32_t)palette_from_fixed(palette, i1 >> 2) << 16) |
                                   (uint32_t)palette_from_fixed(palette, i2 >> 2);

                ((uint32_t*)line)[0] = pixel;
                line += 2;
            }

            if (line < line_end) {
                Fixed ii = (*color)(t, info->width, info->height, xx, yy);
                line[0] = palette_from_fixed(palette, ii >> 2);
                line++;
            }
        }
#else /* !OPTIMIZE_WRITES */
        for (xx = 0; xx < info->width; xx++) {
            Fixed ii = (*color)(t, info->width, info->height, xx, yy);
            line[xx]  = palette_from_fixed(palette, ii / 4);
        }
#endif /* !OPTIMIZE_WRITES */

        // go to next line
        pixels = (char*)pixels + info->stride;
    }
}

void stats_init( Stats*  s )
{
    s->lastTime = now_ms();
    s->firstTime = 0.;
    s->firstFrame = 0;
    s->numFrames  = 0;
}

void stats_startFrame( Stats*  s )
{
    s->frameTime = now_ms();
}

void stats_endFrame( Stats*  s )
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


