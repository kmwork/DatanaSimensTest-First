package ru.datana.siemensopc.utils;

import Moka7.S7;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppOptions;
import ru.datana.siemensopc.config.EnumSiemensDataType;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Битовые преобразование сдвигов и бинарных AND
 */
@Slf4j
public class BitOperationsUtils {

    private static String PREFIX_LOG = "[Приведение к типу] ";

    public static BigDecimal doBitsOperations(byte[] data, AppOptions appOptions) throws AppException {

        if (data == null || data.length == 0) {
            log.info(PREFIX_LOG + " Пустые данные");
            return null;
        }

        BigDecimal result;
        EnumSiemensDataType type = appOptions.getDataType();
        if (type == EnumSiemensDataType.TYPE_BIT) {
            boolean bitBoolean = S7.GetBitAt(data, 0, appOptions.getIntBitPosition());
            result = new BigDecimal(bitBoolean ? 1 : 0);
        } else if (type == EnumSiemensDataType.TYPE_BYTE) {
            result = new BigDecimal((int) data[0]);
        } else if (type == EnumSiemensDataType.TYPE_UNSIGNED_WORD) {
            int valueWord = S7.GetWordAt(data, 0);
            result = new BigDecimal(valueWord);
        } else if (type == EnumSiemensDataType.TYPE_UNSIGNED_DOUBLE_WORD) {
            long valueLong = S7.GetDWordAt(data, 0);
            result = new BigDecimal(valueLong);
        } else if (type == EnumSiemensDataType.TYPE_REAL) {
            float valueFloat = S7.GetFloatAt(data, 0);
            result = new BigDecimal(valueFloat);
        } else {
            String args = "тип = " + type + " значение в байт = " + Arrays.toString(data);
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " Не определен тип данных", args, null);
        }

        log.info(PREFIX_LOG + "[Тип: " + type + "] = " + result);
        return result;
    }
}
