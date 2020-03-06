package ru.datana.siemensopc.utils;

import Moka7.S7;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.EnumFormatBytesType;

import java.util.Arrays;

@Slf4j
public class FormatUtils {

    private static final String PREFIX_LOG = "[Dump] ";

    public static void formatBytes(byte[] buffer, EnumFormatBytesType typeFormat) {
        log.info(PREFIX_LOG + " ============== Начало блока ==============");
        if (buffer == null || buffer.length == 0) {
            log.warn(PREFIX_LOG + "<ПУСТО>");
            return;
        }


        if (typeFormat == EnumFormatBytesType.CLASSIC) {
            log.info(PREFIX_LOG + " десятичные числа по байтам: " + Arrays.toString(buffer));

        } else {
            int r = 0;
            StringBuilder hexStringBuffer = new StringBuilder();

            for (int i = 0; i < buffer.length; i++) {
                int v = (buffer[i] & 0x0FF);
                String hv = Integer.toHexString(v);

                if (hv.length() == 1)
                    hv = "0" + hv + " ";
                else
                    hv = hv + " ";

                hexStringBuffer.append(hv);

                r++;
                if (r == 16) {
                    log.info(PREFIX_LOG + hexStringBuffer + " " + S7.GetPrintableStringAt(buffer, i - 15, 16));
                    hexStringBuffer = new StringBuilder();
                    r = 0;
                }
            }
            int L = hexStringBuffer.length();
            if (L > 0) {
                while (hexStringBuffer.length() < 49)
                    hexStringBuffer.append(" ");
                log.info(PREFIX_LOG + hexStringBuffer + S7.GetPrintableStringAt(buffer, buffer.length - r, r));
            } else
                log.info(PREFIX_LOG + "<ПУСТО>");
        }
        log.info(PREFIX_LOG + " ============== Конец блока ==============");
    }
}
