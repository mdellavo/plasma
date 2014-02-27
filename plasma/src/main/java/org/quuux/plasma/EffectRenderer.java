package org.quuux.plasma;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLSurfaceView;

import static android.opengl.GLES20.*;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public abstract class EffectRenderer implements GLSurfaceView.Renderer, GLWallpaperService.Renderer {

    public int loadTexture(final Bitmap bitmap) {
        int[] textures = new int[1];

        glGenTextures(1, textures, 0);
        glBindTexture(GL_TEXTURE_2D, textures[0]);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);


        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bitmap.getHeight() * bitmap.getWidth() * 4);

        imageBuffer.order(ByteOrder.nativeOrder());

        byte buffer[] = new byte[4];

        for(int i = 0; i < bitmap.getHeight(); i++) {
            for(int j = 0; j < bitmap.getWidth(); j++) {
                int color = bitmap.getPixel(j, i);
                buffer[0] = (byte) Color.red(color);
                buffer[1] = (byte)Color.green(color);
                buffer[2] = (byte)Color.blue(color);
                buffer[3] = (byte)Color.alpha(color);
                imageBuffer.put(buffer);
            }
        }

        imageBuffer.position(0);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);

        return textures[0];
    }

}
