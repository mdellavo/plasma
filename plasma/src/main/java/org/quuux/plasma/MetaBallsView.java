package org.quuux.plasma;

import android.content.Context;
import android.util.AttributeSet;

public class MetaBallsView extends GLEffectView {

    private static final String TAG = Log.buildTag(MetaBallsView.class);


    public MetaBallsView(final Context context) {
        super(context);
        init(context);
    }

    public MetaBallsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {
    }

    @Override
    public Renderer getRenderer() {
        return new MetaBallsRenderer();
    }


}