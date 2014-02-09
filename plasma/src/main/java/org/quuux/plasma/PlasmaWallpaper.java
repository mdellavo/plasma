package org.quuux.plasma;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class PlasmaWallpaper extends EffectWallpaper {
    @Override
    EffectView getEffect() {
        return EffectFactory.getEffect(getApplicationContext(), PlasmaView.class);
    }
}
