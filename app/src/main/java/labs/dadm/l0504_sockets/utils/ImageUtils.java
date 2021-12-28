/*
 * Copyright (c) 2016. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package labs.dadm.l0504_sockets.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static final int GET_IMAGE_FROM_FILE = 0;
    public static final int GET_IMAGE_FROM_URI = 1;

    // Samples an image to reduce its size before displaying it on screen.
    public static Bitmap sampleImage(Context context, int source, Object value) {

        Bitmap bitmap = null;
        try {
            InputStream is = null;
            // Get the image from the file where the Server has stored it
            if (source == GET_IMAGE_FROM_FILE) {
                is = context.openFileInput((String) value);
            }
            // Get the image selected by the Client from its URI
            else if (source == GET_IMAGE_FROM_URI) {
                is = context.getContentResolver().openInputStream((Uri) value);
            }
            // The image will be sampled in two steps:
            // - First: get the image size
            // - Then: do the actual sampling
            if (is != null) {
                // Specify the options for sampling the image
                BitmapFactory.Options options = new BitmapFactory.Options();
                // If inJustDecodeBounds is true it does not return a Bitmap,
                // but provides the image size without allocating memory for its pixels
                options.inJustDecodeBounds = true;
                // Decode the InputStream into a bitmap, although it will be null due to options
                BitmapFactory.decodeStream(is, null, options);
                is.close();

                // Disable inJustDecodeBounds to enable the actual sampling
                options.inJustDecodeBounds = false;
                // Get the sampling size
                int inSampleSize = 1;
                // If the image size is bigger than the required, then it should be sampled
                if ((options.outHeight > 200) || (options.outWidth > 200)) {
                    // Computes half the geight and width of the image
                    final int halfHeight = options.outHeight / 2;
                    final int halfWidth = options.outWidth / 2;
                    // Compute the largest sample size that keeps both
                    // the hight and width of the image larger than the required ones
                    while (((halfHeight / inSampleSize) > 200) &&
                            ((halfWidth / inSampleSize) > 200)) {
                        inSampleSize *= 2;
                    }
                }
                options.inSampleSize = inSampleSize;

                // Get the image again from the file where the Server has stored it
                if (source == GET_IMAGE_FROM_FILE) {
                    is = context.openFileInput((String) value);
                }
                // Get the image selected by the Client again from its URI
                else {
                    is = context.getContentResolver().openInputStream((Uri) value);
                }
                // Decode de InputStream into a bitmap
                bitmap = BitmapFactory.decodeStream(is, null, options);
                is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
