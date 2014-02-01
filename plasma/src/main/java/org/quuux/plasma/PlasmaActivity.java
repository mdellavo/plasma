package org.quuux.plasma;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.Button;

public class PlasmaActivity extends Activity implements View.OnClickListener {

    @Override
    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final View decorView = getWindow().getDecorView();
            final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

        setContentView(R.layout.plasma_layout);

        final Button setWallpaperButton = (Button) findViewById(R.id.set_wallpaper);
        setWallpaperButton.setOnClickListener(this);

        final Button settingsButton = (Button) findViewById(R.id.settings);
        settingsButton.setOnClickListener(this);

        final Button moreButton = (Button)findViewById(R.id.more);
        moreButton.setOnClickListener(this);

        final ViewGroup container = (ViewGroup) findViewById(R.id.plasma_container);
        container.addView(new PlasmaView(this));
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {

            case R.id.set_wallpaper: {
                final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(this, PlasmaService.class));
                startActivity(intent);
            } break;

            case R.id.settings: {
                final Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
            } break;

            case R.id.more: {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://search?q=pub:Quuux%20Software"));
                startActivity(intent);
            } break;

            default:
                break;
        }
    }
}

