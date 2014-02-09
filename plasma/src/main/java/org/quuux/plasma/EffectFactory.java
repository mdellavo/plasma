package org.quuux.plasma;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class EffectFactory {

    private static final String TAG = Log.buildTag(EffectFactory.class);

    public static EffectView getEffect(final Context context, Class<? extends EffectView> klass) {

        EffectView rv = null;
        try {
            Constructor<? extends EffectView> constructor = klass.getConstructor(Context.class);
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
