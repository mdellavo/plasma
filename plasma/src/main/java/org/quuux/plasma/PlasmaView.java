package org.quuux.plasma;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

class PlasmaView extends EffectView {

    static {
        System.loadLibrary("plasma");
    }

    public PlasmaView(final Context context) {
        super(context);
    }

    public PlasmaView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public PlasmaView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void render(final Bitmap bitmap, final long t) {
        renderPlasma(bitmap, t);
    }

    /* implementend by libplasma.so */
    private static native void renderPlasma(final Bitmap bitmap, final long t);

}
