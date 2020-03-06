package ru.datana.siemensopc.utils;

import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppOptions;

import java.util.BitSet;

/**
 * Битовые преобразование сдвигов и бинарных AND
 */
@Slf4j
public class BitOperationsUtils {

    private static String PREFIX_LOG = "[Битовые преобразования] ";

    public static byte[] doBitsOperations(byte[] data, AppOptions appOptions) {

        if (data == null || data.length == 0) {
            log.info(PREFIX_LOG + " Пустые данные");
            return null;
        }


        if (!appOptions.isActiveBitMode()) {
            log.info(PREFIX_LOG + " пропушено");
            return data;
        }

        byte[] resultAnd = new byte[data.length];
        byte[] maskBytes = appOptions.getBytesMaskOperationAnd();
        int maskIndex = 0;
        for (int i = 0; i < data.length; i++) {
            if (maskIndex >= maskBytes.length)
                maskIndex = 0;
            resultAnd[i] = (byte) (data[i] & maskBytes[maskIndex]);
            maskIndex++;
        }

        log.info(PREFIX_LOG + " запушено битовое преобразование");
        BitSet bisSet = BitSet.valueOf(resultAnd);
        bisSet.flip(appOptions.getBitFlipFromIndex(), appOptions.getBitFlipToIndex());
        byte[] result = bisSet.toByteArray();
        FormatUtils.formatBytes("Обработанные биты", result, appOptions.getEnumViewFormatType());
        return result;
    }
}
