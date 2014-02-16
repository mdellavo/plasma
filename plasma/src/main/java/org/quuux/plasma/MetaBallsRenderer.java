package org.quuux.plasma;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

class MetaBallsRenderer implements GLSurfaceView.Renderer, GLWallpaperService.Renderer {

    private static final String TAG = Log.buildTag(MetaBallsRenderer.class);

    private static final int SIZE = 512;
    private static final int RADIUS = SIZE / 2;
    private static final int NUM_BALLS = 75;
    private static final double VELOCITY = 2.5;

    private int mWidth;
    private int mHeight;
    private Bitmap mTexture;
    private List<MetaBall> mBalls = new ArrayList<MetaBall>(NUM_BALLS);


    static class MetaBall {
        int age;
        double x, y;
        double dx, dy;

        public MetaBall(final double x, final double y, final double dx, final double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }
    }


    int mTextureId;
    private FloatBuffer mVertices;
    private FloatBuffer mSizes;
    private FloatBuffer mColors;
    private float[] mHsv = new float[3];

    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        GLHelper.init(gl);

        mTexture = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_4444);
        renderTexture();

        Log.d(TAG, "renderer: %s", GLHelper.getRenderer());
        Log.d(TAG, "extensions: %s", GLHelper.getExtensions());

        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_POINT_SMOOTH);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_TEXTURE);
        gl.glEnable(GL10.GL_BLEND);

        gl.glEnable(GL10.GL_DITHER);
        gl.glEnable(GL10.GL_ALPHA_TEST);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
        gl.glDisable(GL10.GL_DEPTH_TEST);

        gl.glHint(GL10.GL_POINT_SMOOTH_HINT,GL10.GL_NICEST);
        gl.glAlphaFunc(GL10.GL_GREATER, 0.02f);

        gl.glBlendFunc (GL10.GL_SRC_ALPHA, GL10.GL_ONE);

        mVertices = GLHelper.floatBuffer(NUM_BALLS * 3);
        mSizes = GLHelper.floatBuffer(NUM_BALLS);
        mColors = GLHelper.floatBuffer(NUM_BALLS * 4);

        gl.glEnable(GL11.GL_POINT_SPRITE_OES);
        gl.glActiveTexture(GL10.GL_TEXTURE0);

        int[] textures = new int[1];

        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(mTexture.getHeight() * mTexture.getWidth() * 4);

        imageBuffer.order(ByteOrder.nativeOrder());

        byte buffer[] = new byte[4];

        for(int i = 0; i < mTexture.getHeight(); i++) {
            for(int j = 0; j < mTexture.getWidth(); j++) {
                int color = mTexture.getPixel(j, i);
                buffer[0] = (byte) Color.red(color);
                buffer[1] = (byte)Color.green(color);
                buffer[2] = (byte)Color.blue(color);
                buffer[3] = (byte)Color.alpha(color);
                imageBuffer.put(buffer);
            }
        }

        imageBuffer.position(0);

        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, mTexture.getWidth(), mTexture.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, imageBuffer);

        mTextureId = textures[0];

        //((GL11)gl).glPointParameterf(GL11.GL_POINT_SIZE_MAX, 10000.0f);
        //((GL11)gl).glPointParameterf(GL11.GL_POINT_SIZE_MIN, 1.0f);
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {


        gl.glViewport(0, 0, width, height);
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(final GL10 gl) {

        mColors.clear();
        mVertices.clear();
        mSizes.clear();

        mVertices.limit(NUM_BALLS * 3);
        mColors.limit(NUM_BALLS * 4);
        mSizes.limit(NUM_BALLS);

        tick();
        for(int i=0; i<NUM_BALLS; i++) {
            final MetaBall ball = mBalls.get(i);

            mVertices.put((float) ball.x);
            mVertices.put((float) ball.y);
            mVertices.put(0);

            mHsv[0] = ball.age % 360;
            mHsv[1] = 1f;
            mHsv[2] = .5f;

            final int color = Color.HSVToColor(mHsv);

            mColors.put(Color.red(color) / 255f);
            mColors.put(Color.green(color) / 255f);
            mColors.put(Color.blue(color) / 255f);
            mColors.put(1f);

            mSizes.put(SIZE);
        }

        mVertices.position(0);
        mColors.position(0);
        mSizes.position(0);


        gl.glClearColor(0, 0, 0, .001f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluOrtho2D(gl, 0, mWidth, 0, mHeight);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnableClientState(GL11.GL_POINT_SIZE_ARRAY_BUFFER_BINDING_OES);
        gl.glEnableClientState(GL11.GL_POINT_SIZE_ARRAY_OES);
        gl.glEnableClientState(GL11.GL_POINT_SPRITE_OES);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glTexEnvf(GL11.GL_POINT_SPRITE_OES, GL11.GL_COORD_REPLACE_OES,
                GL11.GL_TRUE);

        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColors);
        ((GL11)gl).glPointSizePointerOES(GL10.GL_FLOAT, 0, mSizes);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertices);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
        gl.glDrawArrays(GL10.GL_POINTS, 0, NUM_BALLS); // XXX

        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL11.GL_POINT_SPRITE_OES);
        gl.glDisableClientState(GL11.GL_POINT_SIZE_ARRAY_OES);
        gl.glDisableClientState(GL11.GL_POINT_SIZE_ARRAY_BUFFER_BINDING_OES);
    }

    private double fallOff(final double distance) {
        final double rv;
        if (distance < .333) {
            rv = 1. - (3. * Math.pow(distance, 2.));
        } else if (distance < 1) {
            rv = 1.5 * Math.pow(1 - distance, 2.);
        } else {
            rv = 0;
        }

        return rv;
    }

    private int clamp(double val, double min, double max) {
        return (int) Math.round(Math.max(min, Math.min(max, val)));
    }

    private void renderTexture() {

        Log.d(TAG, "rendering texture");

        mTexture.eraseColor(Color.TRANSPARENT);

        final int r = 255;
        final int g = 255;
        final int b = 255;

        for(int y=0; y<mTexture.getHeight(); y++) {
            for(int x=0; x<mTexture.getWidth(); x++) {

                final double distance = Math.sqrt(Math.pow(RADIUS - (float)x, 2.) + Math.pow(RADIUS - (float)y, 2.)) / (double)RADIUS;
                final double alpha = fallOff(distance);

                final int color = Color.argb(
                        clamp(255. * alpha + .5, 0, 255.),
                        (int) (r - distance), (int) (g - distance), (int) (b - distance)
                );

                mTexture.setPixel(x, y, color);
            }
        }
    }

    public void tick() {
        if (mBalls.size() == 0) {
            for (int i=0; i<NUM_BALLS; i++) {
                final MetaBall ball = new MetaBall(
                        RandomGenerator.randomRange(0, mWidth),
                        RandomGenerator.randomRange(0, mHeight),
                        RandomGenerator.randomRange(-VELOCITY, VELOCITY),
                        RandomGenerator.randomRange(-VELOCITY, VELOCITY)
                );
                ball.age += RandomGenerator.randomInt(0, 50);
                mBalls.add(ball);
            }
        }

        for (int i=0; i<NUM_BALLS; i++) {
            final MetaBall ball = mBalls.get(i);

            ball.x += ball.dx;
            ball.y += ball.dy;

            if (ball.x < 0 || ball.x > mWidth)
                ball.dx *= -1;

            if (ball.y < 0 || ball.y > mHeight)
                ball.dy *= -1;

            ball.age++;
        }
    }

}
