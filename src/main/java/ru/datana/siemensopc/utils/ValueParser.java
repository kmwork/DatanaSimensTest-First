package ru.datana.siemensopc.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class ValueParser {
    private static final String PREFIX_LOG = "[CONFIG] ";

    public static int parseInt(Properties p, String userNameField) throws AppException {
        String strValue = readPropAsText(p, userNameField);
        return parseInt(strValue, userNameField);

    }

    public static String readPropAsText(Properties p, String userNameField, boolean toUpperCase) throws AppException {
        String strValue = p.getProperty(userNameField);
        if (StringUtils.isEmpty(strValue)) {
            String args = userNameField + " = '" + strValue + "'";
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, "пустое значение", args, null);
        }

        String result = strValue.trim();
        if (toUpperCase) result = result.toUpperCase();

        log.info(PREFIX_LOG + ": Чтение значение [" + userNameField + "] = " + result);

        return result;
    }

    public static String readPropAsText(Properties p, String userNameField) throws AppException {
        return readPropAsText(p, userNameField, true);
    }

    public static int parseInt(String strValue, String userNameField) throws AppException {
        log.trace(PREFIX_LOG + ": parse as Int for Field [" + userNameField + "] = " + strValue);
        String args = userNameField + " = '" + strValue + "'";
        if (StringUtils.isEmpty(strValue)) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, "пустое значение", args, null);
        }

        try {
            int value = Integer.parseInt(strValue.trim());
            log.trace(PREFIX_LOG + ": success parsing: [" + userNameField + "] = " + value);
            return value;
        } catch (NumberFormatException ex) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, "не верное целое число", args, ex);
        }

    }


    public static <EN extends Enum<EN>> EN readEnum(Properties p, String userNameField, Class<EN> enumClazz, EN[] allEnumValues) throws AppException {
        String strValue = readPropAsText(p, userNameField, false);
        String args = "as enum : " + userNameField + " = '" + strValue + "'";
        if (!enumClazz.isEnum()) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " не верно указан тип " + enumClazz.getCanonicalName(), args, null);
        }
        try {
            EN value = Enum.valueOf(enumClazz, strValue);
            log.trace(PREFIX_LOG + ": success as enum: [" + userNameField + "] = " + value);
            return value;
        } catch (IllegalArgumentException ex) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " не верно указано значение для перечениления " + enumClazz.getCanonicalName() + " варианты = " + Arrays.toString(allEnumValues), args, ex);
        }
    }


    public static boolean readBoolean(Properties p, String userNameField) throws AppException {
        String strBoolean = readPropAsText(p, userNameField, true);
        String args = "as boolean : " + userNameField + " = '" + strBoolean + "'";
        if (!strBoolean.equals("TRUE") && strBoolean.equals("FALSE"))
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " не верно булево значение  TRUE/FALSE", args, null);
        return strBoolean.equals("TRUE");
    }

    public static byte[] readBytes(Properties p, String userNameField) throws AppException {
        String strBoolean = readPropAsText(p, userNameField, true);

        String args = "как массив бит: значение =" + strBoolean;
        int index = -1;
        String hex = null;
        try {
            String strBytes[] = strBoolean.split(" ");
            byte[] bytes = new byte[strBytes.length];
            for (index = 0; index < strBytes.length; index++) {
                hex = strBytes[index];
                bytes[index] = (byte) Short.parseShort(hex, 16);
            }
            log.debug(PREFIX_LOG + "[BitSet: " + userNameField + " = " + Arrays.toString(bytes));
            return bytes;
        } catch (NumberFormatException ex) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " не верно значение в hex = " + hex + ", index = " + index, args, ex);
        }
    }

}
