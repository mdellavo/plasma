package org.quuux.plasma;

public class MetaBallsWallpaper extends EffectWallpaper {
    @Override
    Class<? extends EffectView> getEffect() {
        return MetaBallsView.class;
    }
}
