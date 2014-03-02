package org.quuux.plasma;

import android.graphics.Bitmap;
import android.opengl.GLU;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SkyBoxRenderer extends EffectRenderer {

    final float[] VERTS = {
            1, -1, 1,
            -1, -1, 1,
            -1, -1, -1,
            1, -1, -1, 
            1, 1, -1,
            1, -1, -1,
            -1, -1, -1, 
            -1, 1, -1, 
            1, 1, 1, 
            1, -1, 1, 
            1, 1, -1, 
            -1, 1, 1, 
            -1, -1, 1, 
            1, 1, 1, 
            -1, 1, 1,
            1, 1, 1, 
            1, 1, -1,
            -1, 1, -1,
            -1, 1, 1,
            -1, 1, -1,
    };
    
    final float[] TEX_COORDS = {
            1, 0,
            0, 0,
            0, 1,
            1, 1,
            0, 0,
            0, 1,
            1, 1,
            1, 0,
            1, 0,
            0, 0,
            1, 1,
            1, 1,
            1, 0,
            0, 1,
            1, 0,
            0, 0,
            0, 1,
            1, 1,
            0, 0,
            0, 1
    };

    final short[] NORTH = { 17, 16, 15, 14 };
    final short[] WEST = { 10, 5, 9, 8 };
    final short[] SOUTH = { 3, 2, 1, 0 };
    final short[] EAST = { 6, 19, 18, 12 };
    final short[] TOP = { 13, 9, 12, 11 };

    final short[][] FACES = { NORTH, WEST, SOUTH, EAST, TOP };

    final ShortBuffer[] mFaces = new ShortBuffer[FACES.length];
    final Bitmap[] mBitmaps;
    final int[] mTextures = new int[FACES.length];
    private FloatBuffer mTexCoords = GLHelper.floatBuffer(TEX_COORDS.length);
    private FloatBuffer mVerts = GLHelper.floatBuffer(VERTS.length);
    private int mTick;

    public SkyBoxRenderer(final Bitmap north, final Bitmap west, final Bitmap south, final Bitmap east, final Bitmap top) {
        mBitmaps = new Bitmap[] { north, west, south, east, top };
    }

    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearDepthf(1.0f);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        mVerts.put(VERTS);
        mVerts.position(0);

        for (int i=0; i<FACES.length; i++) {
            mFaces[i] = GLHelper.shortBuffer(FACES[i].length);
            mFaces[i].put(FACES[i]);
            mFaces[i].position(0);

            mTextures[i] = GLHelper.loadTexture(mBitmaps[i]);
        }

        mTexCoords.put(TEX_COORDS);
        mTexCoords.position(0);
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, .1f, 100f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU.gluLookAt(gl,
                0, 0, 0,
                0, 0, -1,
                0, 1, 0
        );
    }

    @Override
    public void onDrawFrame(final GL10 gl) {

        mTick++;

        gl.glLoadIdentity();

        gl.glColor4f(1f, 1f, 1f, 1f);
        gl.glClearColor(0, 0, 0, .1f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glRotatef(-90, 1, 0, 0);

        gl.glPushMatrix();

        gl.glRotatef(mTick/10.f, 0, 0, 1);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVerts);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoords);

        for (int i=0; i<FACES.length; i++) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[i]);
            gl.glDrawElements(GL10.GL_TRIANGLE_FAN, FACES[i].length,
                    GL10.GL_UNSIGNED_SHORT, mFaces[i]);

        }

        gl.glPopMatrix();

        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
