package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

public class StarFieldView extends EffectView {

    static {
        System.loadLibrary("plasma");
    }

    public StarFieldView(final Context context) {
        super(context);
    }

    public StarFieldView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public StarFieldView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void render(final Bitmap bitmap, final long t) {
        renderStarField(bitmap, t);
    }

    @Override
    protected int getScale() {
        return 1;
    }

    /* implementend by libplasma.so */
    private static native void renderStarField(final Bitmap bitmap, final long t);

}
