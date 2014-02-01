package org.quuux.plasma;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import java.util.Random;

class PlasmaView extends View {

    static {
        System.loadLibrary("plasma");
    }

    private  Bitmap mBitmap;
    private final Random mRandom;
    private final int mWidth;
    private final int mHeight;
    private long mStartTime;
    private final Paint mPaint = new Paint();
    private final Matrix mMatrix = new Matrix();
    private final int mRotate;

    private int mFactor = 4;

    /* implementend by libplasma.so */
    private static native void renderPlasma(Bitmap bitmap, long time_ms);

    public PlasmaView(final Context context, final int width, final int height) {
        super(context);

        mWidth = width;
        mHeight = height;

        allocate();

        mStartTime = System.currentTimeMillis();

        //mPaint.setAntiAlias(true);
        //mPaint.setFilterBitmap(true);
        //mPaint.setDither(true);

        mRandom = new Random();
        mRotate = mRandom.nextInt(360);
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        mMatrix.reset();
        mMatrix.postScale((float)canvas.getWidth() / (float)mBitmap.getWidth(), (float)canvas.getHeight() / (float)mBitmap.getHeight());
        //mMatrix.postRotate(mRotate, mBitmap.getWidth()/2, mBitmap.getHeight()/2);

        final long now = System.currentTimeMillis();
        renderPlasma(mBitmap, now - mStartTime);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        invalidate();
    }

    private void allocate() {
        mBitmap = Bitmap.createBitmap(mWidth / mFactor, mHeight / mFactor, Bitmap.Config.RGB_565);
    }

    public void setResolutionFactor(final int resolutionFactor) {
        if (resolutionFactor != mFactor) {
            mFactor = resolutionFactor;
            allocate();
        }
    }

    public int getResolutionFactor() {
        return mFactor;
    }
}
