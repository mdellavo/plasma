package org.quuux.plasma;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.util.AttributeSet;

abstract public class GLEffectView extends GLSurfaceView {

    public GLEffectView(final Context context) {
        super(context);
        init();
    }

    public GLEffectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        setEGLContextClientVersion(2);
        setRenderer(getRenderer());
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        requestRender();
        invalidate();
    }

    abstract public EffectRenderer getRenderer();

    class LoaderTask extends AsyncTask<Void, Void, EffectRenderer> {
        @Override
        protected EffectRenderer doInBackground(final Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(final EffectRenderer renderer) {
        }
    }
}
