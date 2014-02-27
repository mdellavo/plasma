package org.quuux.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Utils {

    private static final String TAG = Log.buildTag(Utils.class);

    public static Bitmap loadBitmapFromAssets(final Context context, final String path) {
        try {
            final InputStream in = context.getAssets().open(path);
            final Bitmap rv = BitmapFactory.decodeStream(in);
            return rv;
        } catch (Exception e) {
            Log.e(TAG, "error loading bitmap from %s", e, path);
        }

        return null;
    }

    public static String loadTextFromAssets(final Context context, final String path) {
        try {
            final InputStream in = context.getAssets().open(path);


            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final byte[] buffer = new byte[4096];

            int count;
            while((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);

            return out.toString("UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "error loading text from %s", e, path);
        }

        return null;
    }

}
