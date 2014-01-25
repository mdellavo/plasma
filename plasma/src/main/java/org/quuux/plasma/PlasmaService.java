package org.quuux.plasma;

import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

//         setContentView(new PlasmaView(this, display.getWidth() / FACTOR, display.getHeight() / FACTOR));


public class PlasmaService extends WallpaperService {
    private static final String TAG = Log.buildTag(PlasmaService.class);

    static {
        System.loadLibrary("plasma");
    }


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

        private void draw() {

            final SurfaceHolder surfaceHolder = getSurfaceHolder();
            final Canvas canvas = surfaceHolder.lockCanvas();
            onDraw(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);

            if (isVisible())
                scheduleAnimation(16);
        }

        private void onDraw(final Canvas canvas) {
            mView.draw(canvas);
        }


    }

}
