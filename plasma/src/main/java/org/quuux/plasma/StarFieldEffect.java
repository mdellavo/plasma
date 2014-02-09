package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

public class StarFieldEffect extends EffectView {

    static {
        System.loadLibrary("plasma");
    }

    public StarFieldEffect(final Context context) {
        super(context);
    }

    public StarFieldEffect(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public StarFieldEffect(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void render(final Bitmap bitmap, final long t) {
        renderStarField(bitmap, t);
    }

    @Override
    protected int getScale() {
        return 4;
    }

    /* implementend by libplasma.so */
    private static native void renderStarField(final Bitmap bitmap, final long t);


}
