package org.quuux.plasma;

public class FireWallpaper extends EffectWallpaper {
    @Override
    Class<? extends EffectView> getEffect() {
        return FireView.class;
    }
}
