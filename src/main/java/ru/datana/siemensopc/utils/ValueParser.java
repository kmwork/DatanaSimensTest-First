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
        log.debug(PREFIX_LOG + ": parse as Int for Field [" + userNameField + "] = " + strValue);
        String args = userNameField + " = '" + strValue + "'";
        if (StringUtils.isEmpty(strValue)) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, "пустое значение", args, null);
        }

        try {
            int value = Integer.parseInt(strValue.trim());
            log.debug(PREFIX_LOG + ": success parsing: [" + userNameField + "] = " + value);
            return value;
        } catch (NumberFormatException ex) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, "не верное целое число", args, ex);
        }

    }


    public static <EN extends Enum<EN>> EN readEnum(Properties p, String userNameField, Class<EN> enumClazz, EN[] allEnumValues) throws AppException {
        String args = "as enum : " + userNameField + " = '" + userNameField + "'";
        if (!enumClazz.isEnum()) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " не верно указан тип " + enumClazz.getCanonicalName(), args, null);
        }
        String strValue = readPropAsText(p, userNameField);
        try {
            EN value = Enum.valueOf(enumClazz, userNameField);
            log.debug(PREFIX_LOG + ": success as enum: [" + userNameField + "] = " + value);
            return value;
        } catch (IllegalArgumentException ex) {
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " не верно указано значение для перечениления " + enumClazz.getCanonicalName() + " варианты = " + Arrays.toString(allEnumValues), args, ex);
        }
    }


    public static boolean readBoolean(Properties p, String userNameField) throws AppException {
        String args = "as boolean : " + userNameField + " = '" + userNameField + "'";
        String strBoolean = readPropAsText(p, userNameField, true);
        if (!strBoolean.equals("TRUE") && strBoolean.equals("FALSE"))
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, " не верно булево значение  TRUE/FALSE", args, null);
        return strBoolean.equals("TRUE");
    }
}
