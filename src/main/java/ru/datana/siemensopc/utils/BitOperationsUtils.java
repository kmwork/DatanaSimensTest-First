package ru.datana.siemensopc.utils;

import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppOptions;

import java.util.BitSet;

@Slf4j
public class BitOperationsUtils {

    private static String PREFIX_LOG = "[Битовые преобразования] ";

    public static byte[] doBitsOperations(byte[] data, AppOptions appOptions) {
        if (appOptions.isActiveBitMode()) {
            log.info(PREFIX_LOG + " пропушено");
            return data;
        }

        log.info(PREFIX_LOG + " запушено битовое преобразование");
        BitSet bisSet = BitSet.valueOf(data);
        bisSet.and(appOptions.getBitSetMask());
        bisSet.flip(appOptions.getBitFlipFromIndex(), appOptions.getBitFlipToIndex());
        byte[] result = bisSet.toByteArray();
        FormatUtils.formatBytes(result, appOptions.getEnumViewFormatType());
        return result;
    }
}
