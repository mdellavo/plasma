package org.quuux.plasma;

import android.view.View;

public class HeightMapWallpaper extends GLEffectWallpaper {

    @Override
    EffectRenderer getRenderer() {
        return HeightMapRenderer.getInstance(this);
    }
}
