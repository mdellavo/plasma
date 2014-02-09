package org.quuux.plasma;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PlasmaActivity extends Activity implements View.OnClickListener {

    private ViewPager mPager;

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
        mPager = new ViewPager(this);
        mPager.setAdapter(mAdapter);
        container.addView(mPager);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {

            case R.id.set_wallpaper: {
                final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(this, PlasmaWallpaper.class));
                startActivity(intent);
                finish();
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


    private PagerAdapter mAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return EffectFactory.EFFECTS.size();
        }

        @Override
        public boolean isViewFromObject(final View view, final Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final View rv = EffectFactory.getEffect(getApplicationContext(), EffectFactory.EFFECTS.get(position));
            container.addView(rv);
            return rv;
        }

        @Override
        public void destroyItem(final ViewGroup container, final int position, final Object object) {
            container.removeView((View)object);
        }
    };
}

