package com.noticeboardapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kunal Bhavsar on 28/4/16.
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a kilobyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_KB_BI = BigInteger.valueOf(ONE_KB);

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a megabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

    /**
     * The file copy buffer size (30 MB)
     */
    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    private static FileUtils fileUtils;
    private String fileDirectoryPath;

    private FileUtils(Context context) {
        fileDirectoryPath = Environment.getExternalStorageDirectory() + File.separator
                + "NoticeBoardApp" + File.separator;
    }

    public static void init(Context context) {
        fileUtils = new FileUtils(context);
    }

    public static FileUtils getInstance() {
        return fileUtils;
    }

    public Uri getOutputMediaFileUri(String type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    public File getOutputMediaFile(String type) {
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(fileDirectoryPath + timeStamp + "_" + AppPreferences.getInstance().getAppOwnerId()
                + (type.equals(KeyConstants.MEDIA_TYPE_IMAGE) ? ".jpg" : ".pdf"));

        return mediaFile;
    }

    public String scaleAndCompressImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] data = bos.toByteArray();
        try {
            bos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public Bitmap compressAndScaleImageToBitmap(Bitmap sourceImage) {
        float actualHeight = sourceImage.getHeight();
        float actualWidth = sourceImage.getWidth();
        float maxHeight = 600.0f;
        float maxWidth = 800.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;
        int compressionQuality = 30;//50 percent compression

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
            Bitmap.createScaledBitmap(sourceImage, Math.round(actualWidth), Math.round(actualHeight), false);
            sourceImage.compress(Bitmap.CompressFormat.JPEG, compressionQuality, bos);
        }
        return sourceImage;
    }

    public String encodeFileTOBase64(String filepath) throws IOException {
        byte[] bytes = readFile(filepath);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public String decodeFileFromBase64(String data, String filename) throws IOException {
        byte[] pdfAsBytes = Base64.decode(data, 0);

        String imagePath = fileDirectoryPath + filename;
        File filePath = new File(imagePath);
        FileOutputStream os = new FileOutputStream(filePath, true);
        os.write(pdfAsBytes);
        os.close();
        return imagePath;
    }

    public byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    public static void copyFile(final File srcFile, final File destFile,
                                final boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }

        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        final File parentFile = destFile.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && destFile.canWrite() == false) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
            throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            final long size = input.size(); // TODO See IO-386
            long pos = 0;
            long count = 0;
            while (pos < size) {
                final long remain = size - pos;
                count = remain > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : remain;
                final long bytesCopied = output.transferFrom(input, pos, count);
                if (bytesCopied == 0) { // IO-385 - can happen if file is truncated after caching the size
                    break; // ensure we don't loop forever
                }
                pos += bytesCopied;
            }
        } finally {
            if (output != null)
                output.close();
            if (fos != null)
                fos.close();
            if (input != null)
                input.close();
            if (fis != null)
                fis.close();
        }

        final long srcLen = srcFile.length(); // TODO See IO-386
        final long dstLen = destFile.length(); // TODO See IO-386
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }
}
