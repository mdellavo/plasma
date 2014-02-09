package org.quuux.plasma;

public class FireWallpaper extends EffectWallpaper {
    @Override
    EffectView getEffect() {
        return EffectFactory.getEffect(getApplicationContext(), FireView.class);
    }
}
