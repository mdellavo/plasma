package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;


public class SkyBoxView extends GLEffectView {

    private static final String TAG = Log.buildTag(SkyBoxView.class);

    public SkyBoxView(final Context context) {
        super(context);
    }


    // make get renderer async?
    @Override
    public EffectRenderer getRenderer() {
        return SkyBoxRenderer.getInstance(getContext());
    }

}
