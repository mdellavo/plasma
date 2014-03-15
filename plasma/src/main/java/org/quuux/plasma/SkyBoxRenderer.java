package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import static android.opengl.GLES20.*;

public class SkyBoxRenderer extends ShaderEffectRenderer {

    private static final int FACE_NORTH = 0;
    private static final int FACE_WEST = 1;
    private static final int FACE_SOUTH = 2;
    private static final int FACE_EAST = 3;
    private static final int FACE_TOP = 4;

    private static final String MATRIX_LOCATION = "uMatrix";
    private static final String POSITION_LOCATION = "aPosition";
    private static final String TEXTURE_COORD_LOCATION = "aTextureCoord";
    private static final String TEXTURE_LOCATION = "uTexture";

    private final float[] VERTS = {
             1, -1,  1,
            -1, -1,  1,
            -1, -1, -1,
             1, -1, -1,
             1,  1, -1,
             1, -1, -1,
            -1, -1, -1, 
            -1,  1, -1,
             1,  1,  1,
             1, -1,  1,
             1,  1, -1,
            -1,  1,  1,
            -1, -1,  1,
             1,  1,  1,
            -1,  1,  1,
             1,  1,  1,
             1,  1, -1,
            -1,  1, -1,
            -1,  1,  1,
            -1,  1, -1,
    };
    
    private final float[] TEX_COORDS = {
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

    private final short[] NORTH = { 17, 16, 15, 14 };
    private final short[] WEST = { 10, 5, 9, 8 };
    private final short[] SOUTH = { 3, 2, 1, 0 };
    private final short[] EAST = { 6, 19, 18, 12 };
    private final short[] TOP = { 13, 9, 12, 11 };

    private final short[][] FACES = { NORTH, WEST, SOUTH, EAST, TOP };

    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mModelViewProjectionMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mTmpMatrix = new float[16];

    private final ShortBuffer[] mFaces = new ShortBuffer[FACES.length];
    private final int[] mTextures = new int[FACES.length];
    private final FloatBuffer mTexCoords = GLHelper.floatBuffer(TEX_COORDS.length);
    private final FloatBuffer mVerts = GLHelper.floatBuffer(VERTS.length);

    private int mProgramId;
    private int mPositionLocation;
    private int mTextureLocation;
    private int mTextureCoordLocation;
    private int mMatrixLocation;
    private int mTick;
    private Bitmap[] mBitmaps;

    public SkyBoxRenderer() {}

    private void setFaces(final Bitmap north, final Bitmap west, final Bitmap south, final Bitmap east, final Bitmap top) {
        mBitmaps = new Bitmap[] { north, west, south, east, top };
    }

    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {

        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);

        mProgramId = assembleProgram();

        glUseProgram(mProgramId);

        mMatrixLocation = glGetUniformLocation(mProgramId, MATRIX_LOCATION);
        mTextureLocation = glGetUniformLocation(mProgramId, TEXTURE_LOCATION);

        mPositionLocation = glGetAttribLocation(mProgramId, POSITION_LOCATION);
        mTextureCoordLocation = glGetAttribLocation(mProgramId, TEXTURE_COORD_LOCATION);

        glEnable(GL_DEPTH_TEST);

        glClearDepthf(1.0f);
        glDepthFunc(GL_LEQUAL);

        mVerts.put(VERTS);
        mVerts.position(0);

        glVertexAttribPointer(mPositionLocation, 3, GL_FLOAT, false, 0, mVerts);
        glEnableVertexAttribArray(mPositionLocation);

        mTexCoords.put(TEX_COORDS);
        mTexCoords.position(0);

        glVertexAttribPointer(mTextureCoordLocation, 2, GL_FLOAT, false, 0, mTexCoords);
        glEnableVertexAttribArray(mTextureCoordLocation);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (int i=0; i<FACES.length; i++) {
            mFaces[i] = GLHelper.shortBuffer(FACES[i].length);
            mFaces[i].put(FACES[i]);
            mFaces[i].position(0);

            mTextures[i] = GLHelper.loadTexture(mBitmaps[i]);
        }

        glActiveTexture(GL_TEXTURE0);
        glUniform1i(mTextureLocation, 0);
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -.25f, 1, .5f, 10f);

        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, 0,
                0, 0, -1,
                0, 1, 0
        );
    }

    @Override
    public void onDrawFrame(final GL10 gl) {

        mTick++;

        glClearColor(0, 0, 0, .1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix.setRotateM(mRotationMatrix, 0, -90, 1, 0, 0);
        Matrix.rotateM(mRotationMatrix, 0, mTick / 10f, 0, 0, 1);

        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mTmpMatrix, 0, mModelViewProjectionMatrix, 0, mRotationMatrix, 0);

        glUniformMatrix4fv(mMatrixLocation, 1, false, mTmpMatrix, 0);

        for (int i=0; i<FACES.length; i++) {
            glBindTexture(GL_TEXTURE_2D, mTextures[i]);
            glDrawElements(GL_TRIANGLE_FAN, FACES[i].length, GL_UNSIGNED_SHORT, mFaces[i]);
        }
    }

    public static Bitmap loadBitmap(final Context context, final String face) {
        return Utils.loadBitmapFromAssets(context, String.format("textures/skybox/%s.jpg", face));
    }

    public static SkyBoxRenderer getInstance(final Context context) {
        final SkyBoxRenderer rv = new SkyBoxRenderer();
        rv.setVertextShader(Utils.loadTextFromAssets(context, "shaders/skybox.vertex.glsl"));
        rv.setFragmentShader(Utils.loadTextFromAssets(context, "shaders/skybox.fragment.glsl"));
        rv.setFaces(
                loadBitmap(context, "north"),
                loadBitmap(context, "west"),
                loadBitmap(context, "south"),
                loadBitmap(context, "east"),
                loadBitmap(context, "top")
        );
        return rv;
    }
}
