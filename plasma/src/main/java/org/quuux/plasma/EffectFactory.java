package org.quuux.plasma;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectFactory {

    private static final String TAG = Log.buildTag(EffectFactory.class);

    // TODO
    // fireworks
    // boids :)
    // rain

    public static List<Class<? extends View>> EFFECTS =  Arrays.<Class<? extends View>>asList(
            MetaBallsView.class,
            HeightMapView.class,
            SkyBoxView.class,
            FireView.class,
            PlasmaView.class,
            StarFieldView.class
    );

    public static List<Class<? extends WallpaperService>> WALLPAPERS = Arrays.asList(
            MetaBallsWallpaper.class,
            HeightMapWallpaper.class,
            SkyBoxWallpaper.class,
            FireWallpaper.class,
            PlasmaWallpaper.class,
            StarFieldWallpaper.class
    );

    public static View getEffect(final Context context, Class<? extends View> klass) {

        View rv = null;
        try {
            Constructor<? extends View> constructor = klass.getConstructor(Context.class);
            rv = constructor.newInstance(context);
        } catch (Exception e) {
            Log.e(TAG, "error creating effect: %s", e, klass);
            return rv;
        }

        Log.d(TAG, "created effect %s", rv);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final PlasmaSettingsListener prefLisener = new PlasmaSettingsListener(prefs, rv);
        prefs.registerOnSharedPreferenceChangeListener(prefLisener);

        return rv;
    }

}
