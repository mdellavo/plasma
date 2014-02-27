package org.quuux.plasma;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLU;
import android.opengl.GLUtils;
import static android.opengl.GLES20.*;

import java.nio.ShortBuffer;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

// derived from https://github.com/lithium/android-game
class GLHelper
{
    private static final String TAG = "GLHelper";

    private static String vendor;
    private static String renderer;
    private static String version;
    private static String extensions;

    public static boolean supportsOpenGL20(final Context context) {
        final ActivityManager m = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        return m.getDeviceConfigurationInfo().reqGlEsVersion >= 0x20000;
    }

    public static String getVendor() {
        return glGetString(GL_VENDOR);
    }    

    public static String getRenderer() {
        return glGetString(GL_RENDERER);
    }    

    public static String getVersion() {
        return glGetString(GL_VERSION);
    }    

    public static String getExtensions() {
        return glGetString(GL_EXTENSIONS);
    }    

    private static boolean hasExtension(String extension) {
        return extensions.indexOf(extension) >= 0;        
    }

    public static boolean hasVertexBufferObject() {
        return hasExtension("vertex_buffer_object");
    }

    public static boolean hasDrawTexture() {
        return hasExtension("draw_texture");
    }

    public static boolean hasPointSprite() {
        return hasExtension("point_sprite");
    }

    public static ByteBuffer byteBuffer(int size) {
        ByteBuffer byte_buf = ByteBuffer.allocateDirect(size);
        byte_buf.order(ByteOrder.nativeOrder());
        return byte_buf;
    }

    public static IntBuffer intBuffer(int size) {
        ByteBuffer byte_buf = ByteBuffer.allocateDirect(size * 4);
        byte_buf.order(ByteOrder.nativeOrder());
        return byte_buf.asIntBuffer();
    }

    public static ShortBuffer shortBuffer(int size) {
        ByteBuffer byte_buf = ByteBuffer.allocateDirect(size * 2);
        byte_buf.order(ByteOrder.nativeOrder());
        return byte_buf.asShortBuffer();
    }

    public static FloatBuffer floatBuffer(int size) {
        ByteBuffer byte_buf = ByteBuffer.allocateDirect(size * 4);
        byte_buf.order(ByteOrder.nativeOrder());
        return byte_buf.asFloatBuffer();
    }

    public static FloatBuffer toFloatBuffer(float[] data) {
        FloatBuffer rv = floatBuffer(data.length);

        for(int i=0; i<data.length; i++)
            rv.put(data[i]);
        
        rv.position(0);

        return rv;
    }

    public static FloatBuffer toFloatBuffer(Vector3[] data) {
        FloatBuffer rv = floatBuffer(data.length*3);

        for(int i=0; i<data.length; i++) {
            rv.put(data[i].x);
            rv.put(data[i].y);
            rv.put(data[i].z);
        }

        rv.position(0);

        return rv;
    }

    public static FloatBuffer toFloatBuffer(Vector2[] data) {
        FloatBuffer rv = floatBuffer(data.length*2);

        for(int i=0; i<data.length; i++) {
            rv.put(data[i].x);
            rv.put(data[i].y);
        }

        rv.position(0);

        return rv;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            Log.e(TAG, "%s glError -> %s", glOperation, error);
        }
    }

    private static int createTexture() {
        int[] textures = new int[1];
        glGenTextures(1, textures, 0);

        if (BuildConfig.DEBUG) Log.d(TAG, "Created Texture: " + textures[0]);

        return textures[0];
    }

    public static int loadTexture(final Bitmap bitmap) {
        int texture = createTexture();

        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                GL_NICEST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
                GL_NICEST);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                GL_REPEAT);

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);


        return texture;
    }


    public static int compileShader(int type, String source) {
        final int id = glCreateShader(type);

        checkGlError("glCreateShader");

        if (id == 0) {
            Log.e(TAG, "error creating shader");
            return 0;
        }

        glShaderSource(id, source);
        glCompileShader(id);

        final int[] status = new int[1];
        glGetShaderiv(id, GL_COMPILE_STATUS, status, 0);

        if (status[0] == 0) {
            final String info = glGetShaderInfoLog(id);
            Log.e(TAG, "Error compiling shader %s (status: %s) : %s", id, status[0], info);
            glDeleteShader(id);
            return 0;
        }

        return id;
    }

    public static int linkProgram(final int... shaders) {
        final int id = glCreateProgram();
        if (id == 0) {
            Log.e(TAG, "error creating program");
            return 0;
        }

        for (int i=0; i<shaders.length; i++)
            glAttachShader(id, shaders[i]);

        glLinkProgram(id);

        final int[] status = new int[1];

        glGetProgramiv(id, GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            final String info = glGetProgramInfoLog(id);
            Log.e(TAG, "Error linking program %s (status: %s) : %s", id, status[0], info);
            glDeleteProgram(id);
            return 0;
        }

        return id;
    }

    public static boolean validateProgram(final int id) {
        glValidateProgram(id);
        final int[] status = new int[1];
        glGetProgramiv(id, GL_VALIDATE_STATUS, status, 0);
        final String info = glGetProgramInfoLog(id);
        Log.d(TAG, "validing program - status: %s -> %s", status[0], info);
        return status[0] != 0;
    }

    private static int createBufferObject() {
        int[] rv = new int[1];
        glGenBuffers(1, rv, 0);
        return rv[0];
    }
    
    public static int loadBufferObject(FloatBuffer data) {
        int buffer = createBufferObject();

        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, data.limit() * 4, data,
                GL_STATIC_DRAW);

        return buffer;
    }

    public static Vector3 projectTouchToWorld(int width, int height, float[] modelView, float[] projection, float x, float y) {
        int[] view = new int[] {0, 0, width, height};

        float[] touch_position = new float[4];
        int r = GLU.gluUnProject(x, view[3] - y, 1f,
                modelView, 0,
                projection, 0,
                view, 0,
                touch_position, 0);

        touch_position[0] /= touch_position[3];
        touch_position[1] /= touch_position[3];
        touch_position[2] /= touch_position[3];
        touch_position[3] /= touch_position[3];

        Vector3 rv = new Vector3(touch_position[0], touch_position[1], touch_position[2]);
        rv.normalize();
        return rv;
 }

}
