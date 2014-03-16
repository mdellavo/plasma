package org.quuux.plasma;

import android.content.Context;

public class HeightMapView extends GLEffectView {

    public HeightMapView(final Context context) {
        super(context);
    }

    @Override
    public EffectRenderer getRenderer() {
        return HeightMapRenderer.getInstance(getContext());
    }
}
