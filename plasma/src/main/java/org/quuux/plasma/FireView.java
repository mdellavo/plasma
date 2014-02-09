package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

public class FireView extends EffectView {

    static {
        System.loadLibrary("plasma");
    }

    public FireView(final Context context) {
        super(context);
    }

    public FireView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public FireView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void render(final Bitmap bitmap, final long t) {
        renderFire(bitmap, t);
    }

    @Override
    protected int getScale() {
        return 2;
    }

    /* implementend by libplasma.so */
    private static native void renderFire(final Bitmap bitmap, final long t);


}
