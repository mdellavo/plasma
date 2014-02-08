package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public abstract class EffectView extends View {

    static {
        System.loadLibrary("plasma");
    }

    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private long mStartTime;
    private final Paint mPaint = new Paint();
    private final Matrix mMatrix = new Matrix();

    private int mFactor = 1;

    public EffectView(final Context context) {
        super(context);
        init(context);
    }

    public EffectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EffectView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(final Context context) {
        mStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        recycle();
        allocate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        mMatrix.reset();
        mMatrix.postScale((float)canvas.getWidth() / (float)mBitmap.getWidth(), (float)canvas.getHeight() / (float)mBitmap.getHeight());

        final long now = System.currentTimeMillis();
        render(mBitmap, now - mStartTime);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);

        invalidate();
    }

    protected abstract void render(final Bitmap bitmap, long t);

    private void allocate() {
        mBitmap = Bitmap.createBitmap(mWidth / mFactor, mHeight / mFactor, Bitmap.Config.RGB_565);
    }

    public void recycle() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
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
