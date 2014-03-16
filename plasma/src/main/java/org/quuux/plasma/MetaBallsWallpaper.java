package org.quuux.plasma;

import android.service.wallpaper.WallpaperService;
import android.view.View;

public class MetaBallsWallpaper extends GLEffectWallpaper {
    @Override
    EffectRenderer getRenderer() {
        return MetaBallsRenderer.getInstance(this);
    }
}
