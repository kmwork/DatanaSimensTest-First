package ru.datana.siemensopc.executer;

import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.github.s7connector.exception.S7Exception;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppOptions;
import ru.datana.siemensopc.utils.AppException;
import ru.datana.siemensopc.utils.BitOperationsUtils;
import ru.datana.siemensopc.utils.FormatUtils;

/**
 * Движок от GitHub (упрещенное апи - поключил через зависимость мавен)
 * <p>
 * взято из s7connector
 * https://github.com/s7connector/s7connector
 */
@Slf4j
public class S7GithubExecutor implements IExecutor {
    private AppOptions appOptions;

    public S7GithubExecutor(AppOptions appOptions) {
        this.appOptions = appOptions;
    }

    @Override
    public void run() throws AppException {

        try {

            int successCount = 0;
            int errorCount = 0;
            for (int index = 1; successCount < appOptions.getIntLoopCount(); index++) {
                int step = index + 1;
                try {
                    S7Connector connector =
                            S7ConnectorFactory
                                    .buildTCPConnector()
                                    .withHost(appOptions.getIpHost())
                                    .withRack(appOptions.getIntRack()) //optional
                                    .withSlot(appOptions.getIntSlot()) //optional
                                    .build();

                    //Read from DB100 10 bytes
                    byte[] dataBytes = connector.read(appOptions.getEnumS7DaveAreaType(), appOptions.getIntS7DBNumber(), appOptions.getIntBytes(), appOptions.getIntOffset());
                    successCount++;
                    FormatUtils.formatBytes("ОРИГИНАЛ", dataBytes, appOptions.getEnumViewFormatType());
                    BitOperationsUtils.doBitsOperations(dataBytes, appOptions);
                    log.info("[Data: Шаг = " + step + ", Успешных шагов = " + successCount + " ]");
                    Thread.sleep(appOptions.getIntStepPauseMS());
                } catch (S7Exception s7) {
                    log.error("[ERROR-DATA: Шаг = " + step + ", Ошибка", s7);
                    errorCount++;
                }
            }


            log.info("[ИТОГ: Успешных шагов = " + successCount + ", Ошибочных попыток =  " + errorCount);
        } catch (InterruptedException e) {
            log.error("[App-Error: Аварийное завершение программы: ", e);
        }

    }
}