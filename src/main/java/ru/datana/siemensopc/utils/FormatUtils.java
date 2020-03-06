package ru.datana.siemensopc.utils;

import Moka7.S7;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.EnumFormatBytesType;

import java.util.Arrays;

/**
 * Формирование байтов в лог
 * в hex и десятичном формате
 */
@Slf4j
public class FormatUtils {


    public static void formatBytes(String methodName, byte[] buffer, EnumFormatBytesType typeFormat) {
        String prefixLog = "[DUMP] [Источник:" + methodName + "] ";

        log.info(prefixLog + " ============== Начало блока ==============");
        if (buffer == null || buffer.length == 0) {
            log.warn(prefixLog + "<ПУСТО>");
            return;
        }


        if (typeFormat == EnumFormatBytesType.CLASSIC) {
            log.info(prefixLog + " десятичные числа по байтам: " + Arrays.toString(buffer));

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
                    log.info(prefixLog + hexStringBuffer + " " + S7.GetPrintableStringAt(buffer, i - 15, 16));
                    hexStringBuffer = new StringBuilder();
                    r = 0;
                }
            }
            int L = hexStringBuffer.length();
            if (L > 0) {
                while (hexStringBuffer.length() < 49)
                    hexStringBuffer.append(" ");
                log.info(prefixLog + hexStringBuffer + S7.GetPrintableStringAt(buffer, buffer.length - r, r));
            } else
                log.info(prefixLog + "<ПУСТО>");
        }
        log.info(prefixLog + " ============== Конец блока ==============");
    }
}
