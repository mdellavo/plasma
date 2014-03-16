package org.quuux.plasma;

import android.graphics.Bitmap;
import android.view.View;


public class SkyBoxWallpaper extends GLEffectWallpaper {
    @Override
    EffectRenderer getRenderer() {
        return SkyBoxRenderer.getInstance(this);
    }
}
