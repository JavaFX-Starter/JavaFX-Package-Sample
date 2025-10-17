package com.icuxika.util;

import java.text.DecimalFormat;

public class FormatUtil {

    public static String fileSize2String(long size) {
        double result;

        if (size < 1024) return size + "Byte";

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        result = (double) size;
        result /= 1024.0;
        if (result < 1024) return decimalFormat.format(result) + "KiB";
        size /= 1024;

        result = (double) size;
        result /= 1024.0;
        if (result < 1024) return decimalFormat.format(result) + "MiB";
        size /= 1024;

        result = (double) size;
        result /= 1024.0;
        if (result < 1024) return decimalFormat.format(result) + "GiB";
        size /= 1024;

        result = (double) size;
        result /= 1024.0;
        if (result < 1024) return decimalFormat.format(result) + "TiB";
        size /= 1024;

        result = (double) size;
        result /= 1024.0;
        if (result < 1024) return decimalFormat.format(result) + "PiB";
        size /= 1024;

        result = (double) size;
        result /= 1024.0;
        if (result < 1024) return decimalFormat.format(result) + "EiB";
        size /= 1024;

        result = (double) size;
        result /= 1024.0;
        if (result < 1024) return decimalFormat.format(result) + "ZiB";

        return "超级大";
    }
}
