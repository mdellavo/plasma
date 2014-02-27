package org.quuux.plasma;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.Matrix;

import static android.opengl.GLES20.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


class MetaBallsRenderer extends EffectRenderer {

    private static final String TAG = Log.buildTag(MetaBallsRenderer.class);

    private static final int SIZE = 256;
    private static final int RADIUS = SIZE / 2;
    private static final int NUM_BALLS = 250;
    private static final double VELOCITY = 2.5;

    private static final String POSITION_LOCATION = "aPosition";
    private static final String COLOR_LOCATION = "uColor";
    private static final String SIZES_LOCATION = "uSize";
    private static final String MATRIX_LOCATION = "uMatrix";

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

    private int mWidth;
    private int mHeight;
    private Bitmap mTexture;
    private List<MetaBall> mBalls = new ArrayList<MetaBall>(NUM_BALLS);

    int mTextureId;

    private FloatBuffer mVertices = GLHelper.floatBuffer(NUM_BALLS * 3);
    private FloatBuffer mSizes= GLHelper.floatBuffer(NUM_BALLS);
    private FloatBuffer mColors = GLHelper.floatBuffer(NUM_BALLS * 4);
    private float[] mHsv = new float[3];
    private float[] mMatrix = new float[16];

    private String mFragmentShader;
    private String mVertexShader;
    private int mProgramId;
    private int mPositionLocation;
    private int mColorLocation;
    private int mSizesLocation;
    private int mMatrixLocation;

    public void setFragmentShader(final String shader) {
        Log.d(TAG, "fragment shader = %s", shader);
        mFragmentShader = shader;
    }

    public void setVertextShader(final String shader) {
        Log.d(TAG, "vertex shader = %s", shader);
        mVertexShader = shader;
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        Log.d(TAG, "renderer: %s", GLHelper.getRenderer());
        Log.d(TAG, "extensions: %s", GLHelper.getExtensions());

        mTexture = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        renderTexture();

        //mTextureId = loadTexture(mTexture);

        mProgramId = GLHelper.linkProgram(
                GLHelper.compileShader(GL_FRAGMENT_SHADER, mFragmentShader),
                GLHelper.compileShader(GL_VERTEX_SHADER, mVertexShader)
        );
        GLHelper.validateProgram(mProgramId);

        glUseProgram(mProgramId);

        mPositionLocation = glGetAttribLocation(mProgramId, POSITION_LOCATION);
        mColorLocation = glGetUniformLocation(mProgramId, COLOR_LOCATION);
        mSizesLocation = glGetUniformLocation(mProgramId, SIZES_LOCATION);
        mMatrixLocation = glGetUniformLocation(mProgramId, MATRIX_LOCATION);

        glUniform4f(mColorLocation, 1, 1, 1, 1);
        glUniform1f(mSizesLocation, 10);

        glVertexAttribPointer(mPositionLocation, 3, GL_FLOAT, false, 0, mVertices);
        glEnableVertexAttribArray(mPositionLocation);
    }

    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
        glViewport(0, 0, width, height);

        mWidth = width;
        mHeight = height;

        Matrix.orthoM(mMatrix, 0, -1, 1, 1, -1, 1, -1);
        glUniformMatrix4fv(mMatrixLocation, 1, false, mMatrix, 0);

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

    @Override
    public void onDrawFrame(final GL10 unused) {

        tick();
        render();

        glClearColor(0, 0, 0, .1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDrawArrays(GL_POINTS, 0, NUM_BALLS);
    }

    private void render() {
        mColors.clear();
        mVertices.clear();
        mSizes.clear();

        mVertices.limit(NUM_BALLS * 3);
        mColors.limit(NUM_BALLS * 4);
        mSizes.limit(NUM_BALLS);

        for(int i=0; i<NUM_BALLS; i++) {
            final MetaBall ball = mBalls.get(i);

            mVertices.put((float) ball.x / (float)SIZE);
            mVertices.put((float) ball.y / (float)SIZE);
            mVertices.put(0);

            mHsv[0] = ball.age % 360;
            mHsv[1] = 1f;
            mHsv[2] = .5f;

            final int color = Color.HSVToColor(mHsv);

            mColors.put(Color.red(color) / 255f);
            mColors.put(Color.green(color) / 255f);
            mColors.put(Color.blue(color) / 255f);
            mColors.put(.6f);

            mSizes.put(SIZE);
        }

        mVertices.position(0);
        mColors.position(0);
        mSizes.position(0);

    }

    private double fallOff(final double distance) {
        return distance < 1 ? Math.pow(1 - Math.pow(distance, 2.f), 2.f) : 0;
    }

    private int clamp(double val, double min, double max) {
        return (int) Math.round(Math.max(min, Math.min(max, val)));
    }

    private void renderTexture() {

        Log.d(TAG, "rendering texture");

        mTexture.eraseColor(Color.BLACK);

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
        for (int i=0; i<NUM_BALLS; i++) {
            final MetaBall ball = mBalls.get(i);

            ball.x += ball.dx;
            ball.y += ball.dy;

            if (ball.x < -mWidth/2)
                ball.dx = Math.abs(ball.dx);
            else if (ball.x > mWidth/2)
                ball.dx = -Math.abs(ball.dx);

            if (ball.y < -mWidth/2)
                ball.dy = Math.abs(ball.dy);
            else if (ball.y > mHeight/2)
                ball.dy = -Math.abs(ball.dy);

            ball.age++;
        }
    }

}
