package ru.datana.siemensopc.utils;

import ru.datana.siemensopc.config.AppOptions;

import java.util.Arrays;
import java.util.BitSet;

public class BitOperationsUtils {

    private static int SIZE_64bits = 8;

    public static byte[] doBitsOperations(byte[] data, AppOptions options) {
        String args = "data = " + Arrays.toString(data);

        BitSet bisSet = BitSet.valueOf(data);
        bisSet.and(options.getBitSetMask());
        bisSet.flip(options.getBitFlipFromIndex(), options.getBitFlipToIndex());
        byte[] result = bisSet.toByteArray();
        FormatUtils.formatBytes(result, options.getEnumViewFormatType());
        return result;
    }
}
