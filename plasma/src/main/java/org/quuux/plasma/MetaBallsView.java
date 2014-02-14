package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MetaBallsView extends EffectView {

    private static final int SIZE = 512;
    private static final int RADIUS = SIZE / 2;
    private static final int NUM_BALLS = 25;
    private static final String TAG = Log.buildTag(MetaBallsView.class);
    private static final double VELOCITY = 10.;

    private Matrix mMatrix;
    private Paint mPaint;

    static class MetaBall {
        double x, y;
        double dx, dy;

        public MetaBall(final double x, final double y, final double dx, final double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }
    }

    private Bitmap mTexture;
    private int mSize;

    private List<MetaBall> mBalls = new ArrayList<MetaBall>(NUM_BALLS);

    public MetaBallsView(final Context context) {
        super(context);
        init();
    }

    public MetaBallsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MetaBallsView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mSize = SIZE;
        mTexture = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
        renderTexture();

        mMatrix = new Matrix();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        mPaint.setColorFilter(new LightingColorFilter(0x01FFFFFF, 0));

    }

    private double fallOff(final double distance) {
        final double rv;
        if (distance < .333) {
            rv = 1. - (3. * Math.pow(distance, 2.));
        } else if (distance < 1.) {
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

        mTexture.eraseColor(Color.BLACK);

        final int r = RandomGenerator.randomInt(0, 255);
        final int g = RandomGenerator.randomInt(0, 255);
        final int b = RandomGenerator.randomInt(0, 255);

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

    public void onDraw(final Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        if (mBalls.size() == 0) {
            for (int i=0; i<NUM_BALLS; i++) {
                final MetaBall ball = new MetaBall(
                        RandomGenerator.randomRange(0,  canvas.getWidth() - RADIUS),
                        RandomGenerator.randomRange(0, canvas.getHeight() - RADIUS),
                        RandomGenerator.randomRange(-VELOCITY, VELOCITY),
                        RandomGenerator.randomRange(-VELOCITY, VELOCITY)
                );
                mBalls.add(ball);
            }
        }

        for (int i=0; i<NUM_BALLS; i++) {
            final MetaBall ball = mBalls.get(i);

            ball.x += ball.dx;
            ball.y += ball.dy;

            if (ball.x < -RADIUS || ball.x > canvas.getWidth() - RADIUS)
                ball.dx *= -1;

            if (ball.y < -RADIUS || ball.y > canvas.getHeight() - RADIUS)
                ball.dy *= -1;

            //Log.d(TAG, "blit ball at %.02f,%.02f", ball.x, ball.y);

            mMatrix.reset();
            mMatrix.setTranslate((float) ball.x, (float) ball.y);
            canvas.drawBitmap(mTexture, mMatrix, mPaint);
        }

        invalidate();

    }

    @Override
    protected void render(final Bitmap bitmap, final long t) {}

    @Override
    protected int getScale() {
        return 1;
    }

}