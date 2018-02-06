package com.yjy.tnloader.TNLoader.Engine.Decoder;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.yjy.tnloader.TNLoader.Cache.BitmapPool.BitmapPool;
import com.yjy.tnloader.TNLoader.Cache.ByteArrayPool;
import com.yjy.tnloader.TNLoader.Request.Target;
import com.yjy.tnloader.TNLoader.Resource.DecodeFormat;
import com.yjy.tnloader.TNLoader.Utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Queue;
import java.util.Set;

/**
 * Created by software1 on 2018/2/2.
 */

public abstract class LowMemoryDecoder implements BitmapDecoder<InputStream> {

    private static final String TAG = "LowMemoryDecoder";

    private static final Queue<BitmapFactory.Options> OPTIONS_QUEUE = Util.createQueue(0);
    private static final int MARK_POSITION = 5 * 1024 * 1024;

    private static final Set<ImageHeaderParser.ImageType> TYPES_THAT_USE_POOL = EnumSet.of(
            ImageHeaderParser.ImageType.JPEG, ImageHeaderParser.ImageType.PNG_A, ImageHeaderParser.ImageType.PNG);

    public static ImageHeaderParser.ImageType imageType;

    /**
     * Load and scale the image uniformly (maintaining the image's aspect ratio) so that the smallest edge of the
     * image will be between 1x and 2x the requested size. The larger edge has no maximum size.
     */
    public static final LowMemoryDecoder AT_LEAST = new LowMemoryDecoder() {
        @Override
        protected int getSampleSize(int inWidth, int inHeight, int outWidth, int outHeight) {
            return Math.min(inHeight / outHeight, inWidth / outWidth);
        }

        @Override
        public String getId() {
            return "AT_LEAST.com.yjy.tnloader.TNLoader.Engine.Decoder";
        }
    };

    /**
     * Load and scale the image uniformly (maintaining the image's aspect ratio) so that largest edge of the image
     * will be between 1/2x and 1x of the requested size. The smaller edge has no minimum size.
     */
    public static final LowMemoryDecoder AT_MOST = new LowMemoryDecoder() {
        @Override
        protected int getSampleSize(int inWidth, int inHeight, int outWidth, int outHeight) {
            int maxIntegerFactor = (int) Math.ceil(Math.max(inHeight / (float) outHeight,
                    inWidth / (float) outWidth));
            int lesserOrEqualSampleSize = Math.max(1, Integer.highestOneBit(maxIntegerFactor));
            return lesserOrEqualSampleSize << (lesserOrEqualSampleSize < maxIntegerFactor ? 1 : 0);
        }

        @Override
        public String getId() {
            return "AT_MOST.com.yjy.tnloader.TNLoader.Engine.Decoder";
        }
    };

    /**
     * Load the image at its original size.
     */
    public static final LowMemoryDecoder NONE = new LowMemoryDecoder() {
        @Override
        protected int getSampleSize(int inWidth, int inHeight, int outWidth, int outHeight) {
            return 0;
        }

        @Override
        public String getId() {
            return "NONE.com.yjy.tnloader.TNLoader.Engine.Decoder";
        }
    };


    @Override
    public Bitmap decode(InputStream is, BitmapPool pool, int outWidth, int outHeight,DecodeFormat decodeFormat) throws Exception {
        final ByteArrayPool byteArrayPool = ByteArrayPool.get();
        final byte[] bytesForOptions = byteArrayPool.getBytes();
        final byte[] bytesForStream = byteArrayPool.getBytes();
        final BitmapFactory.Options options = getDefaultOptions();

        // Use to fix the mark limit to avoid allocating buffers that fit entire images.
        RecyclableBufferedInputStream bufferedStream = new RecyclableBufferedInputStream(
                is, bytesForStream);
        // Use to retrieve exceptions thrown while reading.
        // TODO(#126): when the framework no longer returns partially decoded Bitmaps or provides a way to determine
        // if a Bitmap is partially decoded, consider removing.
        ExceptionCatchingInputStream exceptionStream =
                ExceptionCatchingInputStream.obtain(bufferedStream);
        // Use to read data.
        // Ensures that we can always reset after reading an image header so that we can still attempt to decode the
        // full image even when the header decode fails and/or overflows our read buffer. See #283.
        MarkEnforcingInputStream invalidatingStream = new MarkEnforcingInputStream(exceptionStream);

        try {
            exceptionStream.mark(MARK_POSITION);
            int orientation = 0;
            try {
                orientation = new ImageHeaderParser(exceptionStream).getOrientation();
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Cannot determine the image orientation from header", e);
                }
            } finally {
                try {
                    exceptionStream.reset();
                } catch (IOException e) {
                    if (Log.isLoggable(TAG, Log.WARN)) {
                        Log.w(TAG, "Cannot reset the input stream", e);
                    }
                }
            }

            options.inTempStorage = bytesForOptions;

            final int[] inDimens = getDimensions(invalidatingStream, bufferedStream, options);
            final int inWidth = inDimens[0];
            final int inHeight = inDimens[1];

            final int degreesToRotate = TransformationUtils.getExifOrientationDegrees(orientation);
            final int sampleSize = getRoundedSampleSize(degreesToRotate, inWidth, inHeight, outWidth, outHeight);

            final Bitmap downsampled =
                    downsampleWithSize(invalidatingStream, bufferedStream, options, pool, inWidth, inHeight, sampleSize,
                            decodeFormat);

            // BitmapFactory swallows exceptions during decodes and in some cases when inBitmap is non null, may catch
            // and log a stack trace but still return a non null bitmap. To avoid displaying partially decoded bitmaps,
            // we catch exceptions reading from the stream in our ExceptionCatchingInputStream and throw them here.
            final Exception streamException = exceptionStream.getException();
            if (streamException != null) {
                throw new RuntimeException(streamException);
            }

            Bitmap rotated = null;
            if (downsampled != null) {
                rotated = TransformationUtils.rotateImageExif(downsampled, pool, orientation);

                if (!downsampled.equals(rotated) && !pool.put(downsampled)) {
                    downsampled.recycle();
                }
            }

            return rotated;
        } finally {
            byteArrayPool.releaseBytes(bytesForOptions);
            byteArrayPool.releaseBytes(bytesForStream);
            exceptionStream.release();
            releaseOptions(options);
        }


    }





    private int getRoundedSampleSize(int degreesToRotate, int inWidth, int inHeight, int outWidth, int outHeight) {
        int targetHeight = outHeight == Target.SIZE_ORIGINAL ? inHeight : outHeight;
        int targetWidth = outWidth == Target.SIZE_ORIGINAL ? inWidth : outWidth;

        final int exactSampleSize;
        if (degreesToRotate == 90 || degreesToRotate == 270) {
            // If we're rotating the image +-90 degrees, we need to downsample accordingly so the image width is
            // decreased to near our target's height and the image height is decreased to near our target width.
            //noinspection SuspiciousNameCombination
            exactSampleSize = getSampleSize(inHeight, inWidth, targetWidth, targetHeight);
        } else {
            exactSampleSize = getSampleSize(inWidth, inHeight, targetWidth, targetHeight);
        }

        // BitmapFactory only accepts powers of 2, so it will round down to the nearest power of two that is less than
        // or equal to the sample size we provide. Because we need to estimate the final image width and height to
        // re-use Bitmaps, we mirror BitmapFactory's calculation here. For bug, see issue #224. For algorithm see
        // http://stackoverflow.com/a/17379704/800716.
        final int powerOfTwoSampleSize = exactSampleSize == 0 ? 0 : Integer.highestOneBit(exactSampleSize);

        // Although functionally equivalent to 0 for BitmapFactory, 1 is a safer default for our code than 0.
        return Math.max(1, powerOfTwoSampleSize);
    }

    private Bitmap downsampleWithSize(MarkEnforcingInputStream is, RecyclableBufferedInputStream  bufferedStream,
                                      BitmapFactory.Options options, BitmapPool pool, int inWidth, int inHeight, int sampleSize,
                                      DecodeFormat decodeFormat) {
        // Prior to KitKat, the inBitmap size must exactly match the size of the bitmap we're decoding.
        Bitmap.Config config = getConfig(is, decodeFormat);
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = config;
        if ((options.inSampleSize == 1 || Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) && shouldUsePool(is)) {
            int targetWidth = (int) Math.ceil(inWidth / (double) sampleSize);
            int targetHeight = (int) Math.ceil(inHeight / (double) sampleSize);
            // BitmapFactory will clear out the Bitmap before writing to it, so getDirty is safe.
            setInBitmap(options, pool.getDirty(targetWidth, targetHeight, config));
        }
        return decodeStream(is, bufferedStream, options);
    }

    private static boolean shouldUsePool(InputStream is) {
        // On KitKat+, any bitmap can be used to decode any other bitmap.
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            return true;
        }

        is.mark(1024);
        try {
            final ImageHeaderParser.ImageType type = new ImageHeaderParser(is).getType();
            // cannot reuse bitmaps when decoding images that are not PNG or JPG.
            // look at : https://groups.google.com/forum/#!msg/android-developers/Mp0MFVFi1Fo/e8ZQ9FGdWdEJ
            imageType = type;
            return TYPES_THAT_USE_POOL.contains(type);
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Cannot determine the image type from header", e);
            }
        } finally {
            try {
                is.reset();
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Cannot reset the input stream", e);
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static Bitmap.Config getConfig(InputStream is, DecodeFormat format) {
        // Changing configs can cause skewing on 4.1, see issue #128.
        if (format == DecodeFormat.ALWAYS_ARGB_8888 || format == DecodeFormat.PREFER_ARGB_8888
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            return Bitmap.Config.ARGB_8888;
        }

        boolean hasAlpha = false;
        // We probably only need 25, but this is safer (particularly since the buffer size is > 1024).
        is.mark(1024);
        try {
            hasAlpha = new ImageHeaderParser(is).hasAlpha();
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Cannot determine whether the image has alpha or not from header for format " + format, e);
            }
        } finally {
            try {
                is.reset();
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Cannot reset the input stream", e);
                }
            }
        }

        return hasAlpha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
    }

    protected abstract int getSampleSize(int inWidth, int inHeight, int outWidth, int outHeight);

    /**
     * A method for getting the dimensions of an image from the given InputStream.
     *
     * @param is The InputStream representing the image.
     * @param options The options to pass to
     *          {@link BitmapFactory#decodeStream(java.io.InputStream, android.graphics.Rect,
     *              android.graphics.BitmapFactory.Options)}.
     * @return an array containing the dimensions of the image in the form {width, height}.
     */
    public int[] getDimensions(MarkEnforcingInputStream is, RecyclableBufferedInputStream bufferedStream,
                               BitmapFactory.Options options) {
        options.inJustDecodeBounds = true;
        decodeStream(is, bufferedStream, options);
        options.inJustDecodeBounds = false;
        return new int[] { options.outWidth, options.outHeight };
    }


    private static Bitmap decodeStream(MarkEnforcingInputStream is, RecyclableBufferedInputStream bufferedStream,
                                       BitmapFactory.Options options) {
        if (options.inJustDecodeBounds) {
            // This is large, but jpeg headers are not size bounded so we need something large enough to minimize
            // the possibility of not being able to fit enough of the header in the buffer to get the image size so
            // that we don't fail to load images. The BufferedInputStream will create a new buffer of 2x the
            // original size each time we use up the buffer space without passing the mark so this is a maximum
            // bound on the buffer size, not a default. Most of the time we won't go past our pre-allocated 16kb.
            is.mark(MARK_POSITION);
        } else {
            // Once we've read the image header, we no longer need to allow the buffer to expand in size. To avoid
            // unnecessary allocations reading image data, we fix the mark limit so that it is no larger than our
            // current buffer size here. See issue #225.
            bufferedStream.fixMarkLimit();
        }
        //解析webP数据
        Bitmap result = null;
        if(imageType != null&&imageType == ImageHeaderParser.ImageType.WEBP){
            try {
                byte[] bytes = toByteArray(is);
                result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,options);
            }catch (IOException e){
                e.printStackTrace();
            }

        }else {
            result = BitmapFactory.decodeStream(is, null, options);
        }


        try {
            if (options.inJustDecodeBounds) {
                is.reset();
            }
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.ERROR)) {
                Log.e(TAG, "Exception loading inDecodeBounds=" + options.inJustDecodeBounds
                        + " sample=" + options.inSampleSize, e);
            }
        }

        return result;
    }

    /**decode webp**/
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while (-1 != (n = input.read(buffer))) {
            byteArrayOutputStream.write(buffer, 0, n);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void setInBitmap(BitmapFactory.Options options, Bitmap recycled) {
        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
            options.inBitmap = recycled;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static synchronized BitmapFactory.Options getDefaultOptions() {
        BitmapFactory.Options decodeBitmapOptions;
        synchronized (OPTIONS_QUEUE) {
            decodeBitmapOptions = OPTIONS_QUEUE.poll();
        }
        if (decodeBitmapOptions == null) {
            decodeBitmapOptions = new BitmapFactory.Options();
            resetOptions(decodeBitmapOptions);
        }

        return decodeBitmapOptions;
    }

    private static void releaseOptions(BitmapFactory.Options decodeBitmapOptions) {
        resetOptions(decodeBitmapOptions);
        synchronized (OPTIONS_QUEUE) {
            OPTIONS_QUEUE.offer(decodeBitmapOptions);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void resetOptions(BitmapFactory.Options decodeBitmapOptions) {
        decodeBitmapOptions.inTempStorage = null;
        decodeBitmapOptions.inDither = false;
        decodeBitmapOptions.inScaled = false;
        decodeBitmapOptions.inSampleSize = 1;
        decodeBitmapOptions.inPreferredConfig = null;
        decodeBitmapOptions.inJustDecodeBounds = false;
        decodeBitmapOptions.outWidth = 0;
        decodeBitmapOptions.outHeight = 0;
        decodeBitmapOptions.outMimeType = null;

        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)  {
            decodeBitmapOptions.inBitmap = null;
            decodeBitmapOptions.inMutable = true;
        }
    }


}
