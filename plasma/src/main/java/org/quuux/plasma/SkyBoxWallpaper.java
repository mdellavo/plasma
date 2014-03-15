package org.quuux.plasma;

import android.graphics.Bitmap;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;


public class SkyBoxWallpaper extends GLWallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new SkyBoxEngine();
    }

    class SkyBoxEngine extends GLEngine {
        SkyBoxEngine() {
            setRenderer(SkyBoxRenderer.getInstance(getApplicationContext()));
        }
    }

}
