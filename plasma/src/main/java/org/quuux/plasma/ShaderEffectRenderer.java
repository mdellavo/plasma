package org.quuux.plasma;

import static android.opengl.GLES20.*;

public abstract class ShaderEffectRenderer extends EffectRenderer {

    private static final String TAG = Log.buildTag(ShaderEffectRenderer.class);
    private String mFragmentShader;
    private String mVertexShader;

    public void setFragmentShader(final String shader) {
        Log.d(TAG, "fragment shader = %s", shader);
        mFragmentShader = shader;
    }

    public void setVertextShader(final String shader) {
        Log.d(TAG, "vertex shader = %s", shader);
        mVertexShader = shader;
    }


    public int assembleProgram() {
        final int programId = GLHelper.linkProgram(
                GLHelper.compileShader(GL_FRAGMENT_SHADER, mFragmentShader),
                GLHelper.compileShader(GL_VERTEX_SHADER, mVertexShader)
        );
        GLHelper.validateProgram(programId);

        return programId;
    }
}
