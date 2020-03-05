package ru.datana.siemensopc.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.moka.contants.EnumS7Area;
import ru.datana.siemensopc.utils.AppException;
import ru.datana.siemensopc.utils.LanitFileUtils;
import ru.datana.siemensopc.utils.ValueParser;

import java.util.Properties;

@ToString
@Slf4j
public class AppOptions {

    @Getter
    private String ipHost;

    @Getter
    private int intRack;

    @Getter
    private int intSlot;

    @Getter
    private int intBytes;

    @Getter
    private int intOffset;
    @Getter
    private int intLoopCount;
    @Getter
    private int intS7DBNumber;

    @Getter
    private EnumS7Area enumS7Area;

    @Getter
    private EnumAppWorkMode appWorkMode;


    @Getter
    private boolean appMakeAllTests;

    @Getter
    private int intStepPauseMS;

    public void load() throws AppException {
        Properties p = LanitFileUtils.readDataConfig();
        ipHost = ValueParser.readPropAsText(p, "host");
        intRack = ValueParser.parseInt(p, "rack");
        intSlot = ValueParser.parseInt(p, "slot");
        intBytes = ValueParser.parseInt(p, "bytes.count");
        intOffset = ValueParser.parseInt(p, "offset");
        intLoopCount = ValueParser.parseInt(p, "loop.count");
        intS7DBNumber = ValueParser.parseInt(p, "s7.db.number");
        enumS7Area = ValueParser.readEnum(p, "s7.area", EnumS7Area.class, EnumS7Area.values());
        appWorkMode = ValueParser.readEnum(p, "app.work.mode", EnumAppWorkMode.class, EnumAppWorkMode.values());
        appMakeAllTests = ValueParser.readBoolean(p, "app.make.all.tests");
        intStepPauseMS = ValueParser.parseInt(p, "step.pause.ms");

        log.info("[Настройки] Параметры = " + toString());

    }
}
