package org.quuux.plasma;

public class StarFieldWallpaper extends EffectWallpaper {
    @Override
    EffectView getEffect() {
        return EffectFactory.getEffect(getApplicationContext(), StarFieldView.class);
    }
}
