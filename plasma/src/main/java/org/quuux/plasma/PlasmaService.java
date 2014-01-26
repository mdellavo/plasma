package org.quuux.plasma;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

//         setContentView(new PlasmaView(this, display.getWidth() / FACTOR, display.getHeight() / FACTOR));


public class PlasmaService extends WallpaperService {
    private static final String TAG = Log.buildTag(PlasmaService.class);

    @Override
    public Engine onCreateEngine() {
        return new PlasmaEngine();
    }

    class PlasmaEngine extends WallpaperService.Engine {
        private PlasmaView mView;
        private final Handler mHandler = new Handler();

        private final Runnable mDrawRunnable = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        @Override
        public void onSurfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mView = new PlasmaView(getApplicationContext(), width, height);
        }

        @Override
        public void onSurfaceDestroyed(final SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            stopAnimation();
        }

        @Override
        public void onVisibilityChanged(final boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible)
                startAnimation();
            else
                stopAnimation();
        }

        private void stopAnimation() {
            Log.d(TAG, "stop animation");
            mHandler.removeCallbacks(mDrawRunnable);
        }

        private void startAnimation() {
            Log.d(TAG, "start animation");
            mHandler.post(mDrawRunnable);
        }

        private void scheduleAnimation(final long delay) {
            mHandler.removeCallbacks(mDrawRunnable);
            mHandler.postDelayed(mDrawRunnable, delay);
        }

        @SuppressLint("WrongCall")
        private void draw() {

            final long t1 = System.currentTimeMillis();
            final SurfaceHolder surfaceHolder = getSurfaceHolder();
            final Canvas canvas = surfaceHolder.lockCanvas();
            onDraw(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);
            final long t2 = System.currentTimeMillis();


            final long duration = t2 - t1;
            //Log.d(TAG, "draw took %dms", duration);

            if (isVisible())
                scheduleAnimation(Math.min(100 - duration, 0));
        }

        private void onDraw(final Canvas canvas) {
            mView.draw(canvas);
        }


    }

}
