package ru.datana.siemensopc.executer;

import Moka7.*;
import com.github.s7connector.exception.S7Exception;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppConts;
import ru.datana.siemensopc.config.AppOptions;
import ru.datana.siemensopc.config.EnumAppWorkMode;
import ru.datana.siemensopc.config.EnumFormatBytesType;
import ru.datana.siemensopc.utils.AppException;
import ru.datana.siemensopc.utils.BitOperationsUtils;
import ru.datana.siemensopc.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Движок на Moka7
 * на базе кода из
 */
@Slf4j
public class Moka7Executor implements IExecutor {


    private long Elapsed;
    private final S7Client clientS7 = new S7Client();
    private int successCount = 0;
    private int failedCount = 0;
    private int DataToMove; // Data size to read/write
    private int CurrentStatus = S7.S7CpuStatusUnknown;

    private final AppOptions appOptions;

    public Moka7Executor(AppOptions appOptions) {
        this.appOptions = appOptions;
    }

    private String prefixMethod(String functionName) {
        return "[Method: " + functionName + "] ";
    }

    private void TestBegin(String functionName) {
        String prefix = prefixMethod(functionName);
        log.info(prefix + "==== старт ==== ");
        Elapsed = System.currentTimeMillis();
    }

    private void TestEnd(String functionName, int result) {
        String prefix = prefixMethod(functionName);
        if (result != 0) {
            failedCount++;
            Error(functionName, result);
        } else
            successCount++;
        log.info(prefix + " === Время выполнения = " + (System.currentTimeMillis() - Elapsed) + " ms");
    }

    private void Error(String functionName, int errorCode) {
        String prefix = prefixMethod(functionName) + "[Error] ";
        int sysErrorCode = clientS7.LastError;
        log.error(prefix + " Код ошибки = " + errorCode + ". Системный код ошибки = +" + sysErrorCode + ". Описание: " + S7Client.ErrorText(sysErrorCode));
    }

    private void BlockInfo(int BlockType, int BlockNumber) {
        S7BlockInfo Block = new S7BlockInfo();
        String method = "Чтение блока";
        TestBegin(method);
        int result = -1;
        try {

            result = clientS7.GetAgBlockInfo(BlockType, BlockNumber, Block);
            if (result == 0) {
                log.info(" ====================== Block Flags ======================");
                log.info("Block Flags     : " + Integer.toBinaryString(Block.BlkFlags()));
                log.info("Block Number    : " + Block.BlkNumber());
                log.info("Block Languege  : " + Block.BlkLang());
                log.info("Load Size       : " + Block.LoadSize());
                log.info("SBB Length      : " + Block.SBBLength());
                log.info("Local Data      : " + Block.LocalData());
                log.info("MC7 Size        : " + Block.MC7Size());
                log.info("Author          : " + Block.Author());
                log.info("Family          : " + Block.Family());
                log.info("Header          : " + Block.Header());
                log.info("Version         : " + Block.Version());
                log.info("Checksum        : 0x" + Integer.toHexString(Block.Checksum()));
                SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy");
                log.info("Code Date       : " + ft.format(Block.CodeDate()));
                log.info("Interface Date  : " + ft.format(Block.IntfDate()));
                log.info(" ______________________ Block Flags  (конец) ______________________");
            }
        } finally {
            TestEnd(method, result);
        }

    }

    private boolean readDataS7() {
        String method = "Чтение данных - ReadArea (datana edition)";
        TestBegin(method);
        byte[] dataBytes = new byte[appOptions.getIntBytes()];
        int result = -1;
        try {
            result = clientS7.ReadArea(appOptions.getEnumS7DaveAreaType().getCode(), appOptions.getIntS7DBNumber(), appOptions.getIntOffset(), appOptions.getIntBytes(), dataBytes);

        } finally {
            TestEnd(method, result);
        }

        if (result == 0) {
            log.info("DB " + appOptions.getIntS7DBNumber() + " - Size read " + appOptions.getIntBytes() + " bytes");
            FormatUtils.formatBytes("Оригинальные байты", dataBytes, appOptions.getEnumViewFormatType());
            BitOperationsUtils.doBitsOperations(dataBytes, appOptions);
            return true;
        }
        return false;
    }

    private void DBRead(byte[] buffer) {
        String method = "чтение блока";
        TestBegin(method);
        int result = -1;

        try {
            clientS7.ReadArea(appOptions.getEnumMoka7AreaType().getS7AreaCode(), appOptions.getIntS7DBNumber(), appOptions.getIntOffset(), DataToMove, buffer);
        } finally {
            TestEnd(method, result);
        }
        if (result == 0) {
            log.info(AppConts.SUCCESS_LOG_PREFIX + "Прочитано из getEnumS7Area = " + appOptions.getEnumMoka7AreaType().getUserDesc());
        }
        TestEnd(method, result);
    }

    private void DBWrite(byte[] buffer) {
        String method = "записиь данных";
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.WriteArea(appOptions.getEnumMoka7AreaType().getS7AreaCode(), appOptions.getIntS7DBNumber(), appOptions.getIntOffset(), buffer.length, buffer);
        } finally {
            TestEnd(method, result);
        }
        if (result == 0) {
            log.info(AppConts.SUCCESS_LOG_PREFIX + "Записано из getEnumS7Area = " + appOptions.getEnumMoka7AreaType().getUserDesc());

        }
    }

    /**
     * Performs read and write on a given DB
     */
    private void DBPlay() {
        // We use DBSample (default = DB 1) as DB Number
        // modify it if it doesn't exists into the CPU.
        byte[] buffer = new byte[appOptions.getIntBytes()];
        if (readDataS7()) {
            DBRead(buffer);
            if (appOptions.isAppMakeAllTests())
                DBWrite(buffer);
        }
    }

    private void Delay() {
        try {
            Thread.sleep(appOptions.getIntStepPauseMS());
        } catch (InterruptedException e) {


        }
    }

    public void ShowStatus() {
        String method = "чтение статуса";
        String prefix = prefixMethod(method);
        IntByRef PlcStatus = new IntByRef(S7.S7CpuStatusUnknown);
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.GetPlcStatus(PlcStatus);
        } finally {
            TestEnd(method, result);
        }
        if (result == 0) {
            StringBuilder stringBuilder = new StringBuilder("PLC Status : ");
            switch (PlcStatus.Value) {
                case S7.S7CpuStatusRun:
                    stringBuilder.append("RUN");
                    break;
                case S7.S7CpuStatusStop:
                    stringBuilder.append("STOP");
                    break;
                default:
                    stringBuilder.append("Unknown (" + PlcStatus.Value + ")");
            }
            log.info(prefix + " Получен статус = " + stringBuilder);
        }
        CurrentStatus = PlcStatus.Value;

    }

    public void DoRun() {

        String method = "PlcHotStart()";
        TestBegin(method);
        int result = -1;
        try {
            clientS7.PlcHotStart();
        } finally {
            TestEnd(method, result);
        }
        if (result == 0)
            log.info("PLC Started");
    }

    public void DoStop() {
        String method = "PlcStop()";
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.PlcStop();

            if (result == 0)
                log.info("PLC Stopped");
        } finally {
            TestEnd(method, result);
        }
    }

    public void RunStop() {
        switch (CurrentStatus) {
            case S7.S7CpuStatusRun:
                DoStop();
                Delay();
                DoRun();
                break;
            case S7.S7CpuStatusStop:
                DoRun();
                Delay();
                DoStop();
        }
    }

    public void GetSysInfo() {
        int result = -1;
        String method = "получение инфо о прошивке";

        TestBegin(method);
        S7OrderCode OrderCode = new S7OrderCode();

        try {
            result = clientS7.GetOrderCode(OrderCode);
            if (result == 0) {
                log.info("Order Code        : " + OrderCode.Code());
                log.info("Firmware version  : " + OrderCode.V1 + "." + OrderCode.V2 + "." + OrderCode.V3);
            }
        } finally {
            TestEnd(method, result);
        }

        String method2 = "получение инфо о процессоре";
        TestBegin(method2);
        S7CpuInfo CpuInfo = new S7CpuInfo();
        try {
            result = clientS7.GetCpuInfo(CpuInfo);
            if (result == 0) {
                log.info("Module Type Name  : " + CpuInfo.ModuleTypeName());
                log.info("Serial Number     : " + CpuInfo.SerialNumber());
                log.info("AS Name           : " + CpuInfo.ASName());
                log.info("CopyRight         : " + CpuInfo.Copyright());
                log.info("Module Name       : " + CpuInfo.ModuleName());
            }
        } finally {
            TestEnd(method2, result);
        }


        String method3 = "получение инфо о процессоре (по частотам)";
        TestBegin(method3);
        try {
            S7CpInfo CpInfo = new S7CpInfo();
            result = clientS7.GetCpInfo(CpInfo);
            if (result == 0) {
                log.info("Max PDU Length    : " + CpInfo.MaxPduLength);
                log.info("Max connections   : " + CpInfo.MaxConnections);
                log.info("Max MPI rate (bps): " + CpInfo.MaxMpiRate);
                log.info("Max Bus rate (bps): " + CpInfo.MaxBusRate);
            }
        } finally {
            TestEnd(method3, result);
        }
    }

    public void GetDateAndTime() {
        Date PlcDateTime = new Date();
        String method = "получение процессорного времени";
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.GetPlcDateTime(PlcDateTime);
            if (result == 0)
                log.info("CPU Date/Time : " + PlcDateTime);
        } finally {
            TestEnd(method, result);
        }
    }

    public void SyncDateAndTime() {
        String method = "получение системного времени";
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.SetPlcSystemDateTime();
        } finally {
            TestEnd(method, result);
        }
    }

    public void ReadSzl() {
        String method = "чтение килобайта";
        S7Szl SZL = new S7Szl(1024);
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.ReadSZL(0x0011, 0x0000, SZL);
            if (result == 0) {
                log.info("LENTHDR : " + SZL.LENTHDR);
                log.info("N_DR    : " + SZL.N_DR);
                log.info("Size    : " + SZL.DataSize);

                byte[] forView = Arrays.copyOf(SZL.Data, SZL.DataSize);
                FormatUtils.formatBytes("Мета-Инфа", SZL.Data, EnumFormatBytesType.HEX);
            }
        } finally {
            TestEnd(method, result);
        }
    }

    public void GetProtectionScheme() {
        S7Protection Protection = new S7Protection();
        String method = "Служебный метод по защите";
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.GetProtection(Protection);
            if (result == 0) {
                log.info("sch_schal : " + Protection.sch_schal);
                log.info("sch_par   : " + Protection.sch_par);
                log.info("sch_rel   : " + Protection.sch_rel);
                log.info("bart_sch  : " + Protection.bart_sch);
                log.info("anl_sch   : " + Protection.anl_sch);
            }
        } finally {
            TestEnd(method, result);
        }
    }

    public void Summary() {
        log.info("");
        log.info("+================================================================");
        log.info("Tests performed : " + (successCount + failedCount));
        log.info("Passed          : " + successCount);
        log.info("Failed          : " + failedCount);
        log.info("+================================================================");
    }

    public boolean connectMoka7() {
        String method = "Соединение с контроллером";
        String prefix = prefixMethod(method);
        TestBegin(method);
        int result = -1;
        try {
            //kmwork-rem
            //clientS7.SetConnectionType(S7.OP);
            clientS7.SetConnectionType(appOptions.getEnumConnectionMoka7Type().getCode());
            result = clientS7.ConnectTo(appOptions.getIpHost(), appOptions.getIntRack(), appOptions.getIntSlot());
            if (result == 0) {
                log.info(prefix + "Connected to   : " + appOptions.getIpHost() + " (Rack=" + appOptions.getIntRack() + ", Slot=" + appOptions.getIntSlot() + ")");
                log.info(prefix + "PDU negotiated : " + clientS7.PDULength() + " bytes");
            }
            return result == 0;
        } finally {
            TestEnd(method, result);
        }
    }


    public void performTests() {
        GetSysInfo();
        GetProtectionScheme();
        GetDateAndTime();
        if (appOptions.isAppMakeAllTests())
            SyncDateAndTime();
        ReadSzl();
        ShowStatus();
        if (appOptions.isAppMakeAllTests())
            RunStop();

        //kostya-rem
        //BlockInfo(S7.Block_SFC, 1); // Get SFC 1 info (always present in a CPU)
        DBPlay();
        Summary();
    }

    @Override
    public void run() throws AppException {
        try {
            successCount = 0;
            failedCount = 0;

            boolean isConnect = connectMoka7();
            try {
                if (isConnect) {
                    if (appOptions.getAppWorkMode() == EnumAppWorkMode.TEST)
                        performTests();
                    else if (appOptions.getAppWorkMode() == EnumAppWorkMode.READ)
                        danataReadTest();
                    else
                        log.error(AppConts.ERROR_LOG_PREFIX + "Не определен режим работы: " + appOptions.getAppWorkMode() + "'");
                }
            } finally {
                log.info(AppConts.APP_LOG_PREFIX + " делаем Disconnect");
                clientS7.Disconnect();
            }


        } catch (Exception e) {
            log.error(AppConts.ERROR_LOG_PREFIX + "Аварийное завершение программы: ", e);
        }
    }

    private void danataReadTest() {
        String predfix = prefixMethod("Цикл чтения");
        log.info(predfix + " Кол-во шагов = " + appOptions.getIntLoopCount() + " по " + appOptions.getIntBytes() + " байт");
        int successReadCount = 0;
        int errorReadCount = 0;
        for (int index = 1; successReadCount < appOptions.getIntLoopCount(); index++) {
            int step = index + 1;
            try {
                boolean isOk = readDataS7();
                if (isOk)
                    successReadCount++;
                else errorReadCount++;
                Thread.sleep(appOptions.getIntStepPauseMS());
            } catch (S7Exception | InterruptedException s7) {
                log.error("[ERROR-DATA: Шаг = " + step + ", Ошибка", s7);
                errorReadCount++;
            }
        }


        log.info("[ИТОГ: Успешных шагов = " + successReadCount + ", Ошибочных попыток =  " + errorReadCount);
    }


}
