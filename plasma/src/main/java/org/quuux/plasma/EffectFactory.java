package org.quuux.plasma;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class EffectFactory {

    public interface Listener {
        void effectChanged(final EffectView view);
    }

    public static void getEffect(final Context context, final Listener listener) {
        final EffectView rv = new StarFieldView(context);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final PlasmaSettingsListener prefLisener = new PlasmaSettingsListener(prefs, rv);
        prefs.registerOnSharedPreferenceChangeListener(prefLisener);

        listener.effectChanged(rv);
    }

}
