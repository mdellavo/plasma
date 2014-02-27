package org.quuux.plasma;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class HeightMapWallpaper extends GLWallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new HeightMapEngine();
    }

    private class HeightMapEngine extends GLEngine {
        HeightMapEngine() {
            setRenderer(new HeightMapRenderer(null));
        }
    }
}
