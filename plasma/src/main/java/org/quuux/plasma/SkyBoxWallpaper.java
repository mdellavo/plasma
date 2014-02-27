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
            setRenderer(new SkyBoxRenderer(
                    loadBitmap("north"), loadBitmap("west"), loadBitmap("south"), loadBitmap("east"), loadBitmap("top")
            ));
        }
    }

    private Bitmap loadBitmap(final String face) {
        return Utils.loadBitmapFromAssets(this, String.format("textures/skybox/%s.jpg", face));
    }
}
