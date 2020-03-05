package ru.datana.siemensopc.executer;

import ru.datana.siemensopc.utils.AppException;

public interface IExecutor {
    void run() throws AppException;
}
