package org.quuux.plasma;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import static android.opengl.GLES20.*;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public abstract class EffectRenderer implements GLSurfaceView.Renderer, GLWallpaperService.Renderer {

    private static final String TAG = Log.buildTag(EffectRenderer.class);

    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        Log.d(TAG, "renderer: %s", GLHelper.getRenderer());
        Log.d(TAG, "extensions: %s", GLHelper.getExtensions());
    }
}
