package org.quuux.plasma;

import android.service.wallpaper.WallpaperService;
import android.view.View;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class MetaBallsWallpaper extends GLWallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MetaBallsEngine();
    }

    class MetaBallsEngine extends GLEngine {
        MetaBallsEngine() {
            final MetaBallsRenderer renderer = new MetaBallsRenderer();
            renderer.setVertextShader(Utils.loadTextFromAssets(getApplicationContext(), "shaders/vertex.glsl"));
            renderer.setFragmentShader(Utils.loadTextFromAssets(getApplicationContext(), "shaders/fragment.glsl"));
            setRenderer(renderer);
        }
    }
}
