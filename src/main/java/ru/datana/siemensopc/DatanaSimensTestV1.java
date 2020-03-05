package ru.datana.siemensopc;

import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.github.s7connector.exception.S7Exception;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.utils.AppException;
import ru.datana.siemensopc.utils.ValueParser;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class DatanaSimensTestV1 {
    private static final String CONF_FILE_NAME = "datana_siemens.properties";
    private static final String SYS_DIR_PROP = "app.dir";
    private static final String ENCODING = "UTF8";

    //private static final Logger log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    static {
        System.setProperty("file.encoding", ENCODING);
    }

    public static void main(String[] args) {

        log.info("[DatanaSimensTest] ================ Запуск (Старая версия V1) ================. Аргументы = " + Arrays.toString(args));
        try {
            String dirConf = ValueParser.readPropAsText(System.getProperties(), SYS_DIR_PROP);

            File f = new File(dirConf, CONF_FILE_NAME);
            if (!f.isFile() || !f.exists()) {
                log.error("Файл не найден: " + f.getAbsolutePath());
                System.exit(-100);
            }

            Properties p = new Properties();
            try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), ENCODING));) {
                p.load(fileReader);
            } catch (IOException ex) {
                log.error("Не смог прочитать файл: " + f.getAbsolutePath(), ex);
                System.exit(-200);
            }

            log.info("[CONFIG]" + p);
            String ipHost = ValueParser.readPropAsText(p, "host");
            String enumType = ValueParser.readPropAsText(p, "dave.area");
            DaveArea daveArea = DaveArea.valueOf(enumType);
            int intRack = ValueParser.parseInt(p, "rack");
            int intSlot = ValueParser.parseInt(p, "slot");
            int intBytes = ValueParser.parseInt(p, "bytes.count");
            int intOffset = ValueParser.parseInt(p, "offset");
            int intSleep = ValueParser.parseInt(p, "step.time.ms");
            int intLoopCount = ValueParser.parseInt(p, "loop.count");
            int intAreaNumber = ValueParser.parseInt(p, "area.number");

            int successCount = 0;
            int errorCount = 0;
            for (int index = 1; successCount < intLoopCount; index++) {
                int step = index + 1;
                try {
                    S7Connector connector =
                            S7ConnectorFactory
                                    .buildTCPConnector()
                                    .withHost(ipHost)
                                    .withRack(intRack) //optional
                                    .withSlot(intSlot) //optional
                                    .build();

                    //Read from DB100 10 bytes
                    byte[] bs = connector.read(daveArea, intAreaNumber, intBytes, intOffset);
                    successCount++;
                    log.info("[Data: Шаг = " + step + ", Успешных шагов = " + successCount + " ]" + Arrays.toString(bs));
                    Thread.sleep(intSleep);
                } catch (S7Exception s7) {
                    log.error("[ERROR-DATA: Шаг = " + step + ", Ошибка", s7);
                    errorCount++;
                }
            }


            log.info("[ИТОГ: Успешных шагов = " + successCount + ", Ошибочных попыток =  " + errorCount);
        } catch (AppException | InterruptedException e) {
            log.error("[App-Error: Аварийное завершение программы: ", e);
        }

        log.info("[DatanaSimensTest] ********* Завершение программы *********");
    }
}