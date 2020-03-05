package ru.datana.siemensopc.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

@Slf4j
public class ValueParser {
    private static final String PREFIX_LOG = "[CONFIG] ";

    public static int parseInt(Properties p, String userNameField) throws AppException {
        String strValue = readPropAsText(p, userNameField);
        return parseInt(strValue, userNameField);

    }

    public static String readPropAsText(Properties p, String userNameField) throws AppException {
        String strValue = p.getProperty(userNameField);
        if (StringUtils.isEmpty(strValue)) {
            String args = userNameField + " = '" + strValue + "'";
            throw new AppException(TypeException.INVALID_USER_INPUT_DATA, "пустое значение", args, null);
        }

        log.info(PREFIX_LOG + ": Чтение значение [" + userNameField + "] = " + strValue);

        return strValue;
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


}
