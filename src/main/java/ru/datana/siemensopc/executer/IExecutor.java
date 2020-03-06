package ru.datana.siemensopc.executer;

import ru.datana.siemensopc.utils.AppException;

/**
 * Интерфейс подключаемего модуля библиотеки
 */
public interface IExecutor {
    void run() throws AppException;
}
