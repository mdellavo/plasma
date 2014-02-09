package org.quuux.plasma;

public class PlasmaWallpaper extends EffectWallpaper {
    @Override
    Class<? extends EffectView> getEffect() {
        return PlasmaView.class;
    }
}
