package org.quuux.plasma;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

public class Settings extends PreferenceActivity {

    public static final String SETTING_RESOLUTION = "resolution";
    private static final int DEFAULT_RESOLUTION = 4;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.settings_layout);

    }

    public static int getResolution(final SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(SETTING_RESOLUTION, DEFAULT_RESOLUTION);
    }

}
