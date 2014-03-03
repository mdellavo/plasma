package org.quuux.plasma;

import android.content.Context;
import android.util.AttributeSet;

public class MetaBallsView extends GLEffectView {

    private static final String TAG = Log.buildTag(MetaBallsView.class);

    public MetaBallsView(final Context context) {
        super(context);
    }

    public MetaBallsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public EffectRenderer getRenderer() {
        return MetaBallsRenderer.getInstance(getContext());
    }


}