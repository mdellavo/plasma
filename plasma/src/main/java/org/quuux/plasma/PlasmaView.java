package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by marc on 1/25/14.
 */
class PlasmaView extends View {

    private final Bitmap mBitmap;
    private long mStartTime;
    private final Paint mPaint = new Paint();
    private final Rect mDest = new Rect();

    private int mFactor = 8;

    /* implementend by libplasma.so */
    private static native void renderPlasma(Bitmap bitmap, long time_ms);

    public PlasmaView(Context context, int width, int height) {
        super(context);
        mBitmap = Bitmap.createBitmap(width / mFactor, height / mFactor, Bitmap.Config.RGB_565);
        mStartTime = System.currentTimeMillis();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final long now = System.currentTimeMillis();
        renderPlasma(mBitmap, now - mStartTime);
        mDest.set(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawBitmap(mBitmap, null, mDest, mPaint);
        invalidate();
    }
}
