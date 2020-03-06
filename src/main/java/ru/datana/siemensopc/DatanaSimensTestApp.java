/*
/*=============================================================================|
|  PROJECT Moka7                                                         1.0.2 |
|==============================================================================|
|  Copyright (C) 2013, 2016 Davide Nardella                                    |
|  All rights reserved.                                                        |
|==============================================================================|
|  SNAP7 is free software: you can redistribute it and/or modify               |
|  it under the terms of the Lesser GNU General Public License as published by |
|  the Free Software Foundation, either version 3 of the License, or under     |
|  EPL Eclipse Public License 1.0.                                             |
|                                                                              |
|  This means that you have to chose in advance which take before you import   |
|  the library into your project.                                              |
|                                                                              |
|  SNAP7 is distributed in the hope that it will be useful,                    |
|  but WITHOUT ANY WARRANTY; without even the implied warranty of              |
|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE whatever license you    |
|  decide to adopt.                                                            |
|                                                                              |
|=============================================================================*/
package ru.datana.siemensopc;

/**
 * Тестовое ПО на проверку дачтиков сименс
 * по заданию https://conf.dds.lanit.ru/pages/viewpage.action?pageId=43683525
 * pdf  (tech_task.pdf) с заданием в папке doc-manual
 */

import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppConts;
import ru.datana.siemensopc.config.AppLibraryType;
import ru.datana.siemensopc.config.AppOptions;
import ru.datana.siemensopc.executer.IExecutor;
import ru.datana.siemensopc.executer.Moka7Executor;
import ru.datana.siemensopc.executer.S7GithubExecutor;
import ru.datana.siemensopc.utils.AppException;

import java.util.Arrays;

@Slf4j
public class DatanaSimensTestApp {

    public static void main(String[] args) {
        log.info(AppConts.APP_LOG_PREFIX + "================ Запуск  ================. Аргументы = " + Arrays.toString(args));
        try {
            AppOptions appOptions = new AppOptions();
            try {
                appOptions.load();
            } catch (AppException e) {
                log.error("Ошибка чтения конфига", e);
                return;
            }
            AppLibraryType type = appOptions.getAppLibraryType();
            log.info(AppConts.APP_LOG_PREFIX + "****** Аргорим: " + type + ", Версия программы = " + appOptions.getAppVersion());
            IExecutor executor = null;
            if (type == AppLibraryType.MOKA7)
                executor = new Moka7Executor(appOptions);
            else if (type == AppLibraryType.S7)
                executor = new S7GithubExecutor(appOptions);
            else {
                log.error(AppConts.ERROR_LOG_PREFIX + " Не определен аргорим программы = " + type);
                System.exit(-100);
            }
            executor.run();

        } catch (AppException ex) {
            log.error(AppConts.ERROR_LOG_PREFIX + " Ошибка в программе", ex);
        }
        log.info(AppConts.APP_LOG_PREFIX + "********* Завершение программы *********");
    }

}
