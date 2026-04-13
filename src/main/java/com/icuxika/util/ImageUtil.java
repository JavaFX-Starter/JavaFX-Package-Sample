package com.icuxika.util;

public class ImageUtil {

    public record Dimension(int width, int height) {
    }

    public static Dimension readImageSize(byte[] data, int length) {
        if (length < 10) return null;

        // =====================
        // PNG
        // =====================
        if (isPng(data)) {
            if (length >= 24) {
                int width = readInt(data, 16);
                int height = readInt(data, 20);
                return new Dimension(width, height);
            }
        }

        // =====================
        // GIF
        // =====================
        if (isGif(data)) {
            int width = readShort(data, 6);
            int height = readShort(data, 8);
            return new Dimension(width, height);
        }

        // =====================
        // JPEG
        // =====================
        if (isJpeg(data)) {
            return readJpegSize(data, length);
        }

        // =====================
        // WebP
        // =====================
        if (isWebP(data)) {
            return readWebPSize(data, length);
        }

        return null;
    }

    private static boolean isPng(byte[] data) {
        return data.length >= 8 &&
                data[0] == (byte) 0x89 &&
                data[1] == 0x50 &&
                data[2] == 0x4E &&
                data[3] == 0x47;
    }

    private static boolean isGif(byte[] data) {
        return data.length >= 6 &&
                data[0] == 'G' &&
                data[1] == 'I' &&
                data[2] == 'F';
    }

    private static boolean isJpeg(byte[] data) {
        return data.length >= 2 &&
                data[0] == (byte) 0xFF &&
                data[1] == (byte) 0xD8;
    }

    private static boolean isWebP(byte[] data) {
        return data.length >= 12 &&
                data[0] == 'R' &&
                data[1] == 'I' &&
                data[2] == 'F' &&
                data[3] == 'F' &&
                data[8] == 'W' &&
                data[9] == 'E' &&
                data[10] == 'B' &&
                data[11] == 'P';
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) |
                ((data[offset + 1] & 0xFF) << 16) |
                ((data[offset + 2] & 0xFF) << 8) |
                (data[offset + 3] & 0xFF);
    }

    private static int readShort(byte[] data, int offset) {
        return ((data[offset] & 0xFF)) |
                ((data[offset + 1] & 0xFF) << 8);
    }

    private static Dimension readJpegSize(byte[] data, int length) {
        int i = 2;
        while (i < length - 1) {
            if ((data[i] & 0xFF) != 0xFF) {
                i++;
                continue;
            }

            int marker = data[i + 1] & 0xFF;

            // SOF0 / SOF2
            if (marker == 0xC0 || marker == 0xC2) {
                if (i + 8 < length) {
                    int height = ((data[i + 5] & 0xFF) << 8) | (data[i + 6] & 0xFF);
                    int width = ((data[i + 7] & 0xFF) << 8) | (data[i + 8] & 0xFF);
                    return new Dimension(width, height);
                } else {
                    return null;
                }
            } else {
                if (i + 4 >= length) return null;
                int size = ((data[i + 2] & 0xFF) << 8) | (data[i + 3] & 0xFF);
                i += 2 + size;
            }
        }
        return null;
    }

    private static Dimension readWebPSize(byte[] data, int length) {
        if (length < 30) return null;

        String chunk = new String(data, 12, 4);

        switch (chunk) {
            case "VP8 ":
                int width = ((data[26] & 0xFF) | ((data[27] & 0xFF) << 8)) & 0x3FFF;
                int height = ((data[28] & 0xFF) | ((data[29] & 0xFF) << 8)) & 0x3FFF;
                return new Dimension(width, height);

            case "VP8L":
                width = ((data[21] & 0xFF) | ((data[22] & 0xFF) << 8)) & 0x3FFF;
                height = ((data[22] & 0xFF) >> 6 | ((data[23] & 0xFF) << 2)) & 0x3FFF;
                return new Dimension(width + 1, height + 1);

            case "VP8X":
                width = 1 + ((data[24] & 0xFF) | ((data[25] & 0xFF) << 8) | ((data[26] & 0xFF) << 16));
                height = 1 + ((data[27] & 0xFF) | ((data[28] & 0xFF) << 8) | ((data[29] & 0xFF) << 16));
                return new Dimension(width, height);
        }

        return null;
    }
}
