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
        final MetaBallsRenderer renderer = new MetaBallsRenderer();
        renderer.setVertextShader(Utils.loadTextFromAssets(getContext(), "shaders/vertex.glsl"));
        renderer.setFragmentShader(Utils.loadTextFromAssets(getContext(), "shaders/fragment.glsl"));
        return renderer;
    }


}