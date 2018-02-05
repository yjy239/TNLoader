package com.yjy.tnloader.TNLoader.Engine.Decoder;

/**
 * Created by yjy on 2018/2/2.
 */

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser.ImageType.GIF;
import static com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser.ImageType.JPEG;
import static com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser.ImageType.PNG;
import static com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser.ImageType.PNG_A;
import static com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser.ImageType.UNKNOWN;
import static com.yjy.tnloader.TNLoader.Engine.Decoder.ImageHeaderParser.ImageType.WEBP;

/**
 * A class for parsing the exif orientation and other data from an image header.
 */
public class ImageHeaderParser {
    private static final String TAG = "ImageHeaderParser";

    /**
     * The format of the image data including whether or not the image may include transparent pixels.
     */
    public enum ImageType {
        /** GIF type. */
        GIF(true),
        /** JPG type. */
        JPEG(false),
        /** PNG type with alpha. */
        PNG_A(true),
        /** PNG type without alpha. */
        PNG(false),
        /***WebP**/
        WEBP(true),
        /** Unrecognized type. */
        UNKNOWN(false);
        private final boolean hasAlpha;

        ImageType(boolean hasAlpha) {
            this.hasAlpha = hasAlpha;
        }

        public boolean hasAlpha() {
            return hasAlpha;
        }
    }

    private static final int GIF_HEADER = 0x474946;
    private static final int PNG_HEADER = 0x89504E47;
    private static final int EXIF_MAGIC_NUMBER = 0xFFD8;
    // "MM".
    private static final int MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D;
    // "II".
    private static final int INTEL_TIFF_MAGIC_NUMBER = 0x4949;
    private static final String JPEG_EXIF_SEGMENT_PREAMBLE = "Exif\0\0";
    private static final byte[] JPEG_EXIF_SEGMENT_PREAMBLE_BYTES;
    private static final int SEGMENT_SOS = 0xDA;
    private static final int MARKER_EOI = 0xD9;
    private static final int SEGMENT_START_ID = 0xFF;
    private static final int EXIF_SEGMENT_TYPE = 0xE1;
    private static final int ORIENTATION_TAG_TYPE = 0x0112;
    private static final int[] BYTES_PER_FORMAT = { 0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8 };

    private final StreamReader streamReader;

    /* WebP file header
   0                   1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |      'R'      |      'I'      |      'F'      |      'F'      |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                           File Size                           |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |      'W'      |      'E'      |      'B'      |      'P'      |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
*/
    private static final int WEBP_FILE_HEADER_SIZE = 0x0A;
    private static final String WEBP_FILE_HEADER_RIFF = "RIFF";
    private static final String WEBP_FILE_HEADER_WEBP = "WEBP";

    static {
        byte[] bytes = new byte[0];
        try {
            bytes = JPEG_EXIF_SEGMENT_PREAMBLE.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Ignore.
        }
        JPEG_EXIF_SEGMENT_PREAMBLE_BYTES = bytes;
    }

    public ImageHeaderParser(InputStream is) {
        streamReader = new StreamReader(is);
    }

    // 0xD0A3C68 -> <htm
    // 0xCAFEBABE -> <!DOCTYPE...
    public boolean hasAlpha() throws IOException {
        return getType().hasAlpha();
    }

    public ImageType getType() throws IOException {

        int firstTwoBytes = streamReader.getUInt16();

        //webP 先取出头12位,比较头4位和后4位
        if((firstTwoBytes & 0x0FFF) == WEBP_FILE_HEADER_SIZE){
            boolean isWebPFile = false;
            byte[] bytes = new byte[]{
                    (byte)(firstTwoBytes >> 8 & 0xFF),
                    (byte)(firstTwoBytes & 0xFF)
            };

            isWebPFile = WEBP_FILE_HEADER_RIFF.equals(new String(bytes, 0, 4, "US-ASCII"))
                    && WEBP_FILE_HEADER_WEBP.equals(new String(bytes, 8, 4, "US-ASCII"));
            if(isWebPFile){
                return WEBP;
            }
        }

        // JPEG.
        if (firstTwoBytes == EXIF_MAGIC_NUMBER) {
            return JPEG;
        }

        final int firstFourBytes = firstTwoBytes << 16 & 0xFFFF0000 | streamReader.getUInt16() & 0xFFFF;
        // PNG.
        if (firstFourBytes == PNG_HEADER) {
            // See: http://stackoverflow.com/questions/2057923/how-to-check-a-png-for-grayscale-alpha-color-type
            streamReader.skip(25 - 4);
            int alpha = streamReader.getByte();
            // A RGB indexed PNG can also have transparency. Better safe than sorry!
            return alpha >= 3 ? PNG_A : PNG;
        }

        // GIF from first 3 bytes.
        if (firstFourBytes >> 8 == GIF_HEADER) {
            return GIF;
        }

        return UNKNOWN;
    }

    /**
     * Parse the orientation from the image header. If it doesn't handle this image type (or this is not an image)
     * it will return a default value rather than throwing an exception.
     *
     * @return The exif orientation if present or -1 if the header couldn't be parsed or doesn't contain an orientation
     * @throws IOException
     */
    public int getOrientation() throws IOException {
        final int magicNumber = streamReader.getUInt16();

        if (!handles(magicNumber)) {
            return -1;
        } else {
            byte[] exifData = getExifSegment();
            boolean hasJpegExifPreamble = exifData != null
                    && exifData.length > JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.length;

            if (hasJpegExifPreamble) {
                for (int i = 0; i < JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.length; i++) {
                    if (exifData[i] != JPEG_EXIF_SEGMENT_PREAMBLE_BYTES[i]) {
                        hasJpegExifPreamble = false;
                        break;
                    }
                }
            }

            if (hasJpegExifPreamble) {
                return parseExifSegment(new RandomAccessReader(exifData));
            } else {
                return -1;
            }
        }
    }

    private byte[] getExifSegment() throws IOException {
        short segmentId, segmentType;
        int segmentLength;
        while (true) {
            segmentId = streamReader.getUInt8();

            if (segmentId != SEGMENT_START_ID) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Unknown segmentId=" + segmentId);
                }
                return null;
            }

            segmentType = streamReader.getUInt8();

            if (segmentType == SEGMENT_SOS) {
                return null;
            } else if (segmentType == MARKER_EOI) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Found MARKER_EOI in exif segment");
                }
                return null;
            }

            // Segment length includes bytes for segment length.
            segmentLength = streamReader.getUInt16() - 2;

            if (segmentType != EXIF_SEGMENT_TYPE) {
                long skipped = streamReader.skip(segmentLength);
                if (skipped != segmentLength) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Unable to skip enough data"
                                + ", type: " + segmentType
                                + ", wanted to skip: " + segmentLength
                                + ", but actually skipped: " + skipped);
                    }
                    return null;
                }
            } else {
                byte[] segmentData = new byte[segmentLength];
                int read = streamReader.read(segmentData);
                if (read != segmentLength) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Unable to read segment data"
                                + ", type: " + segmentType
                                + ", length: " + segmentLength
                                + ", actually read: " + read);
                    }
                    return null;
                } else {
                    return segmentData;
                }
            }
        }
    }

    private static int parseExifSegment(RandomAccessReader segmentData) {
        final int headerOffsetSize = JPEG_EXIF_SEGMENT_PREAMBLE.length();

        short byteOrderIdentifier = segmentData.getInt16(headerOffsetSize);
        final ByteOrder byteOrder;
        if (byteOrderIdentifier == MOTOROLA_TIFF_MAGIC_NUMBER) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (byteOrderIdentifier == INTEL_TIFF_MAGIC_NUMBER) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unknown endianness = " + byteOrderIdentifier);
            }
            byteOrder = ByteOrder.BIG_ENDIAN;
        }

        segmentData.order(byteOrder);

        int firstIfdOffset = segmentData.getInt32(headerOffsetSize + 4) + headerOffsetSize;
        int tagCount = segmentData.getInt16(firstIfdOffset);

        int tagOffset, tagType, formatCode, componentCount;
        for (int i = 0; i < tagCount; i++) {
            tagOffset = calcTagOffset(firstIfdOffset, i);

            tagType = segmentData.getInt16(tagOffset);

            // We only want orientation.
            if (tagType != ORIENTATION_TAG_TYPE) {
                continue;
            }

            formatCode = segmentData.getInt16(tagOffset + 2);

            // 12 is max format code.
            if (formatCode < 1 || formatCode > 12) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got invalid format code=" + formatCode);
                }
                continue;
            }

            componentCount = segmentData.getInt32(tagOffset + 4);

            if (componentCount < 0) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Negative tiff component count");
                }
                continue;
            }

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got tagIndex=" + i + " tagType=" + tagType + " formatCode=" + formatCode
                        + " componentCount=" + componentCount);
            }

            final int byteCount = componentCount + BYTES_PER_FORMAT[formatCode];

            if (byteCount > 4) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got byte count > 4, not orientation, continuing, formatCode=" + formatCode);
                }
                continue;
            }

            final int tagValueOffset = tagOffset + 8;

            if (tagValueOffset < 0 || tagValueOffset > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal tagValueOffset=" + tagValueOffset + " tagType=" + tagType);
                }
                continue;
            }

            if (byteCount < 0 || tagValueOffset + byteCount > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal number of bytes for TI tag data tagType=" + tagType);
                }
                continue;
            }

            //assume componentCount == 1 && fmtCode == 3
            return segmentData.getInt16(tagValueOffset);
        }

        return -1;
    }

    private static int calcTagOffset(int ifdOffset, int tagIndex) {
        return ifdOffset + 2 + 12 * tagIndex;
    }

    private static boolean handles(int imageMagicNumber) {
        return (imageMagicNumber & EXIF_MAGIC_NUMBER) == EXIF_MAGIC_NUMBER
                || imageMagicNumber == MOTOROLA_TIFF_MAGIC_NUMBER
                || imageMagicNumber == INTEL_TIFF_MAGIC_NUMBER;
    }

    private static class RandomAccessReader {
        private final ByteBuffer data;

        public RandomAccessReader(byte[] data) {
            this.data = ByteBuffer.wrap(data);
            this.data.order(ByteOrder.BIG_ENDIAN);
        }

        public void order(ByteOrder byteOrder) {
            this.data.order(byteOrder);
        }

        public int length() {
            return data.array().length;
        }

        public int getInt32(int offset) {
            return data.getInt(offset);
        }

        public short getInt16(int offset) {
            return data.getShort(offset);
        }
    }

    private static class StreamReader {
        private final InputStream is;
        //motorola / big endian byte order

        public StreamReader(InputStream is) {
            this.is = is;
        }

        public int getUInt16() throws IOException {
            return  (is.read() << 8 & 0xFF00) | (is.read() & 0xFF);
        }

        public short getUInt8() throws IOException {
            return (short) (is.read() & 0xFF);
        }

        public int getUInt12() throws IOException{
            return ((is.read() << 8 & 0xFF00) | (is.read() & 0xFF) &0x0FFF);
        }

        public void read(byte[] buffer, int byteOffset, int byteCount) throws IOException{
            is.read(buffer,byteOffset,byteCount);
        }

        public void reset() throws IOException{
            is.reset();
        }

        public long skip(long total) throws IOException {
            if (total < 0) {
                return 0;
            }

            long toSkip = total;
            while (toSkip > 0) {
                long skipped = is.skip(toSkip);
                if (skipped > 0) {
                    toSkip -= skipped;
                } else {
                    // Skip has no specific contract as to what happens when you reach the end of
                    // the stream. To differentiate between temporarily not having more data and
                    // having finished the stream, we read a single byte when we fail to skip any
                    // amount of data.
                    int testEofByte = is.read();
                    if (testEofByte == -1) {
                        break;
                    } else {
                        toSkip--;
                    }
                }
            }
            return total - toSkip;
        }

        public int read(byte[] buffer) throws IOException {
            int toRead = buffer.length;
            int read;
            while (toRead > 0 && ((read = is.read(buffer, buffer.length - toRead, toRead)) != -1)) {
                toRead -= read;
            }
            return buffer.length - toRead;
        }

        public int getByte() throws IOException {
            return is.read();
        }
    }
}
