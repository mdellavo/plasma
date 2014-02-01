package org.quuux.plasma;

import android.content.SharedPreferences;

public class PlasmaSettingsListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final SharedPreferences mPrefs;
    private final PlasmaView mView;

    public PlasmaSettingsListener(final SharedPreferences prefs, final PlasmaView view) {
        mPrefs = prefs;
        mView = view;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (Settings.SETTING_RESOLUTION.equals(key)) {
            mView.setResolutionFactor(Settings.getResolution(sharedPreferences));
        }
    }
}
