package ru.datana.siemensopc.utils;

import Moka7.S7;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FormatUtils {

    private static final String PREFIX_LOG = "[HexDump] ";

    public static void hexDump(byte[] Buffer, int Size) {
        log.info(PREFIX_LOG + " ============== Начало блока ==============");
        int r = 0;
        StringBuilder hexStringBuffer = new StringBuilder();

        for (int i = 0; i < Size; i++) {
            int v = (Buffer[i] & 0x0FF);
            String hv = Integer.toHexString(v);

            if (hv.length() == 1)
                hv = "0" + hv + " ";
            else
                hv = hv + " ";

            hexStringBuffer.append(hv);

            r++;
            if (r == 16) {
                log.info(PREFIX_LOG + hexStringBuffer + " " + S7.GetPrintableStringAt(Buffer, i - 15, 16));
                hexStringBuffer = new StringBuilder();
                r = 0;
            }
        }
        int L = hexStringBuffer.length();
        if (L > 0) {
            while (hexStringBuffer.length() < 49)
                hexStringBuffer.append(" ");
            log.info(PREFIX_LOG + hexStringBuffer + S7.GetPrintableStringAt(Buffer, Size - r, r));
        } else
            log.info(PREFIX_LOG + "<ПУСТО>");

        log.info(PREFIX_LOG + " ============== Конец блока ==============");
    }
}
