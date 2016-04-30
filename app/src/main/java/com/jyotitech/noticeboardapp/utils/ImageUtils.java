package com.jyotitech.noticeboardapp.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Kunal Bhavsar on 28/4/16.
 */
public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    public static String scaleAndCompressImage(Bitmap bitmap) {
        float actualHeight = bitmap.getHeight();
        float actualWidth = bitmap.getWidth();
        float maxHeight = 600.0f;
        float maxWidth = 800.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;
        int compressionQuality = 50;//50 percent compression

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                //adjust width according to maxHeight
                imgRatio = maxHeight / actualHeight;
                actualWidth = imgRatio * actualWidth;
                actualHeight = maxHeight;
            } else if (imgRatio > maxRatio) {
                //adjust height according to maxWidth
                imgRatio = maxWidth / actualWidth;
                actualHeight = imgRatio * actualHeight;
                actualWidth = maxWidth;
            } else {
                actualHeight = maxHeight;
                actualWidth = maxWidth;
            }
            Bitmap.createScaledBitmap(bitmap, Math.round(actualWidth), Math.round(actualHeight), false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, bos);
        }

        byte[] data = bos.toByteArray();
        try {
            bos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

}
