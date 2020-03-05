package ru.datana.siemensopc.utils;

import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppConts;

import java.io.*;
import java.util.Properties;

@Slf4j
public class LanitFileUtils {

    public static Properties readDataConfig() throws AppException {
        String dirConf = ValueParser.readPropAsText(System.getProperties(), AppConts.SYS_DIR_PROP, false);

        File f = new File(dirConf, AppConts.CONF_FILE_NAME);
        if (!f.isFile() || !f.exists()) {
            log.error("Файл не найден: " + f.getAbsolutePath());
            System.exit(-100);
        }

        Properties p = new Properties();
        try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), AppConts.ENCODING));) {
            p.load(fileReader);
        } catch (IOException ex) {
            log.error("Не смог прочитать файл: " + f.getAbsolutePath(), ex);
            System.exit(-200);
        }
        log.info("[CONFIG]" + p);

        return p;

    }
}
