package org.quuux.plasma;

public class StarFieldWallpaper extends EffectWallpaper {
    @Override
    Class<? extends EffectView> getEffect() {
        return StarFieldView.class;
    }
}
