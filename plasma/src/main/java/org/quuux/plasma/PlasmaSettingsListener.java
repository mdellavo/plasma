package org.quuux.plasma;

import android.content.SharedPreferences;
import android.view.View;

public class PlasmaSettingsListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final SharedPreferences mPrefs;
    private final View mView;

    public PlasmaSettingsListener(final SharedPreferences prefs, final View view) {
        mPrefs = prefs;
        mView = view;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

    }
}
