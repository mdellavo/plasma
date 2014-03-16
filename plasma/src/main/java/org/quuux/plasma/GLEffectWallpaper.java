package org.quuux.plasma;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

abstract public class GLEffectWallpaper extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new GLEffectEngine();
    }

    private class GLEffectEngine extends Engine {

        private WallpaperGLSurfaceView mView;

        @Override
        public void onCreate(final SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            mView = new WallpaperGLSurfaceView(getApplicationContext());
            // Request an OpenGL ES 2.0 compatible context.
            setEGLContextClientVersion(2);

            // On Honeycomb+ devices, this improves the performance when
            // leaving and resuming the live wallpaper.
            setPreserveEGLContextOnPause(true);

            // Set the renderer to our user-defined renderer.
            setRenderer(getRenderer());
        }

        protected void setRenderer(EffectRenderer renderer) {
            mView.setRenderer(renderer);
        }

        protected void setEGLContextClientVersion(int version) {
            mView.setEGLContextClientVersion(version);
        }

        protected void setPreserveEGLContextOnPause(boolean preserve) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                mView.setPreserveEGLContextOnPause(preserve);
        }

        class WallpaperGLSurfaceView extends GLSurfaceView {
            private static final String TAG = "WallpaperGLSurfaceView";

            WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onDestroy() {
                super.onDetachedFromWindow();
            }
        }
    }

    abstract EffectRenderer getRenderer();

}
