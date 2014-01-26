package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import java.util.Random;

class PlasmaView extends View {

    private final Bitmap mBitmap;
    private final Random mRandom;
    private long mStartTime;
    private final Paint mPaint = new Paint();
    private final Matrix mMatrix = new Matrix();
    private final int mRotate;

    private int mFactor = 16;

    /* implementend by libplasma.so */
    private static native void renderPlasma(Bitmap bitmap, long time_ms);

    public PlasmaView(Context context, int width, int height) {
        super(context);
        mBitmap = Bitmap.createBitmap(width / mFactor, height / mFactor, Bitmap.Config.RGB_565);
        mStartTime = System.currentTimeMillis();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);

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
}
