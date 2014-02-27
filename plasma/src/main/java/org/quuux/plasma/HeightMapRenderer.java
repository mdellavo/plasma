package org.quuux.plasma;

import android.graphics.Bitmap;
import android.opengl.GLU;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class HeightMapRenderer extends EffectRenderer {

    final public static int SIZE = 1024;
    final double mMap[][] = new double[SIZE][SIZE];
    final FloatBuffer mBuffer = GLHelper.floatBuffer(SIZE * SIZE);

    public HeightMapRenderer(final Bitmap bitmap) {
        update(bitmap);
    }

    public void update(final Bitmap bitmap) {
        if (bitmap.getWidth() != SIZE || bitmap.getHeight() != SIZE) {
            throw new IllegalArgumentException(String.format("bitmap must but %dx%d pixels!", SIZE, SIZE));
        }

        for (int x=0; x<SIZE; x++)
            for (int y=0; y<SIZE; y++)
                mMap[x][y] = (double)bitmap.getPixel(x, y) / (double)0xFFFFFFFF;
    }

    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glShadeModel(GL10.GL_SMOOTH);

    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, .1f, 100f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU.gluLookAt(gl,
                0, 1, 1,
                0, 0, 0,
                0, 1, 0
        );

    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        for (int x=0; x<SIZE; x++) {
            for (int y=0; y<SIZE; y++) {

            }
        }
    }
}
