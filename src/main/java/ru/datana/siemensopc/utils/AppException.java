package ru.datana.siemensopc.utils;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Главный exception приложения для перекидования наружу
 */
@Slf4j
@ToString
@Getter
public class AppException extends Exception {
    private final String strArgs;
    private final String msg;
    private final TypeException type;

    public AppException(TypeException type, String msg, String strArgs, Exception ex) {
        super("Type: " + type + ",\n msg=" + msg + "\n strArgs = " + strArgs, ex);
        this.type = type;
        this.msg = msg;
        this.strArgs = strArgs;
    }
}
