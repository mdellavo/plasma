package org.quuux.plasma;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

abstract public class GLEffectView extends GLSurfaceView {


    public GLEffectView(final Context context) {
        super(context);
        init();
    }

    public GLEffectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setRenderer(getRenderer());
    }

    abstract public Renderer getRenderer();


}
