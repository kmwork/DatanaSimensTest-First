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
 * @author Dave Nardella
 */

import Moka7.*;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppOptions;
import ru.datana.siemensopc.config.EnumAppWorkMode;
import ru.datana.siemensopc.utils.AppException;
import ru.datana.siemensopc.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class DatanaSimensTestMoka7 {


    private static final String SUCCESS_LOG_PREFIX = "[App-Ошибка] ";
    private static final String ERROR_LOG_PREFIX = "[App-Успешно] ";
    private static final String APP_LOG_PREFIX = "[App-Danata-Mock7] ";

    private long Elapsed;
    private byte[] Buffer = new byte[65536]; // 64K buffer (maximum for S7400 systems)
    private final S7Client clientS7 = new S7Client();
    private int successCount = 0;
    private int failedCount = 0;
    private int DataToMove; // Data size to read/write
    private int CurrentStatus = S7.S7CpuStatusUnknown;

    private final AppOptions appOptions;

    public DatanaSimensTestMoka7(AppOptions appOptions) {
        this.appOptions = appOptions;
    }


    private void TestBegin(String FunctionName) {
        log.info("");
        log.info("+================================================================");
        log.info("| " + FunctionName);
        log.info("+================================================================");
        Elapsed = System.currentTimeMillis();
    }

    private void TestEnd(int Result) {
        if (Result != 0) {
            failedCount++;
            Error(Result);
        } else
            successCount++;
        log.info("Execution time " + (System.currentTimeMillis() - Elapsed) + " ms");
    }

    private void Error(int erorrCode) {
        log.error("[S7Client-Error] Код ошибки = " + S7Client.ErrorText(erorrCode));
    }

    private void BlockInfo(int BlockType, int BlockNumber) {
        S7BlockInfo Block = new S7BlockInfo();
        TestBegin("GetAgBlockInfo()");

        int Result = clientS7.GetAgBlockInfo(BlockType, BlockNumber, Block);
        if (Result == 0) {
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
        TestEnd(Result);
    }

    private boolean readDataS7() {
        IntByRef SizeRead = new IntByRef(appOptions.getIntBytes());
        TestBegin("DBGet()");
        int Result = clientS7.DBGet(appOptions.getIntS7DBNumber(), Buffer, SizeRead);
        TestEnd(Result);
        if (Result == 0) {
            DataToMove = SizeRead.Value; // Stores DB size for next test
            log.info("DB " + appOptions.getIntS7DBNumber() + " - Size read " + DataToMove + " bytes");
            FormatUtils.hexDump(Buffer, DataToMove);
            return true;
        }
        return false;
    }

    private void DBRead() {
        TestBegin("Чтение данных для s7Area = " + appOptions.getEnumS7Area().getUserDesc());
        int Result = clientS7.ReadArea(appOptions.getEnumS7Area().getS7AreaCode(), appOptions.getIntS7DBNumber(), appOptions.getIntOffset(), DataToMove, Buffer);
        if (Result == 0) {
            log.info(SUCCESS_LOG_PREFIX + "Прочитано из getEnumS7Area = " + appOptions.getEnumS7Area().getUserDesc());
        }
        TestEnd(Result);
    }

    private void DBWrite() {
        TestBegin("Запись данных для s7Area = " + appOptions.getEnumS7Area().getUserDesc());
        int Result = clientS7.WriteArea(appOptions.getEnumS7Area().getS7AreaCode(), appOptions.getIntS7DBNumber(), appOptions.getIntOffset(), DataToMove, Buffer);
        if (Result == 0) {
            log.info(SUCCESS_LOG_PREFIX + "Записано из getEnumS7Area = " + appOptions.getEnumS7Area().getUserDesc());
        }
        TestEnd(Result);
    }

    /**
     * Performs read and write on a given DB
     */
    private void DBPlay() {
        // We use DBSample (default = DB 1) as DB Number
        // modify it if it doesn't exists into the CPU.
        if (readDataS7()) {
            DBRead();
            if (appOptions.isAppMakeAllTests())
                DBWrite();
        }
    }

    private void Delay() {
        try {
            Thread.sleep(appOptions.getIntStepPauseMS());
        } catch (InterruptedException e) {
        }
    }

    public void ShowStatus() {
        IntByRef PlcStatus = new IntByRef(S7.S7CpuStatusUnknown);
        TestBegin("GetPlcStatus()");
        int Result = clientS7.GetPlcStatus(PlcStatus);
        if (Result == 0) {
            System.out.print("PLC Status : ");
            switch (PlcStatus.Value) {
                case S7.S7CpuStatusRun:
                    log.info("RUN");
                    break;
                case S7.S7CpuStatusStop:
                    log.info("STOP");
                    break;
                default:
                    log.info("Unknown (" + PlcStatus.Value + ")");
            }
        }
        CurrentStatus = PlcStatus.Value;
        TestEnd(Result);
    }

    public void DoRun() {
        TestBegin("PlcHotStart()");
        int Result = clientS7.PlcHotStart();
        if (Result == 0)
            log.info("PLC Started");
        TestEnd(Result);
    }

    public void DoStop() {
        TestBegin("PlcStop()");
        int Result = clientS7.PlcStop();
        if (Result == 0)
            log.info("PLC Stopped");
        TestEnd(Result);
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
        int Result;
        TestBegin("GetOrderCode()");
        S7OrderCode OrderCode = new S7OrderCode();
        Result = clientS7.GetOrderCode(OrderCode);
        if (Result == 0) {
            log.info("Order Code        : " + OrderCode.Code());
            log.info("Firmware version  : " + OrderCode.V1 + "." + OrderCode.V2 + "." + OrderCode.V3);
        }
        TestEnd(Result);

        TestBegin("GetCpuInfo()");
        S7CpuInfo CpuInfo = new S7CpuInfo();
        Result = clientS7.GetCpuInfo(CpuInfo);
        if (Result == 0) {
            log.info("Module Type Name  : " + CpuInfo.ModuleTypeName());
            log.info("Serial Number     : " + CpuInfo.SerialNumber());
            log.info("AS Name           : " + CpuInfo.ASName());
            log.info("CopyRight         : " + CpuInfo.Copyright());
            log.info("Module Name       : " + CpuInfo.ModuleName());
        }
        TestEnd(Result);

        TestBegin("GetCpInfo()");
        S7CpInfo CpInfo = new S7CpInfo();
        Result = clientS7.GetCpInfo(CpInfo);
        if (Result == 0) {
            log.info("Max PDU Length    : " + CpInfo.MaxPduLength);
            log.info("Max connections   : " + CpInfo.MaxConnections);
            log.info("Max MPI rate (bps): " + CpInfo.MaxMpiRate);
            log.info("Max Bus rate (bps): " + CpInfo.MaxBusRate);
        }
        TestEnd(Result);
    }

    public void GetDateAndTime() {
        Date PlcDateTime = new Date();
        TestBegin("GetPlcDateTime()");
        int Result = clientS7.GetPlcDateTime(PlcDateTime);
        if (Result == 0)
            log.info("CPU Date/Time : " + PlcDateTime);
        TestEnd(Result);
    }

    public void SyncDateAndTime() {
        TestBegin("SetPlcSystemDateTime()");
        int Result = clientS7.SetPlcSystemDateTime();
        TestEnd(Result);
    }

    public void ReadSzl() {
        S7Szl SZL = new S7Szl(1024);
        TestBegin("ReadSZL() - ID : 0x0011, IDX : 0x0000");
        int Result = clientS7.ReadSZL(0x0011, 0x0000, SZL);
        if (Result == 0) {
            log.info("LENTHDR : " + SZL.LENTHDR);
            log.info("N_DR    : " + SZL.N_DR);
            log.info("Size    : " + SZL.DataSize);
            FormatUtils.hexDump(SZL.Data, SZL.DataSize);
        }
        TestEnd(Result);
    }

    public void GetProtectionScheme() {
        S7Protection Protection = new S7Protection();
        TestBegin("GetProtection()");
        int Result = clientS7.GetProtection(Protection);
        if (Result == 0) {
            log.info("sch_schal : " + Protection.sch_schal);
            log.info("sch_par   : " + Protection.sch_par);
            log.info("sch_rel   : " + Protection.sch_rel);
            log.info("bart_sch  : " + Protection.bart_sch);
            log.info("anl_sch   : " + Protection.anl_sch);
        }
        TestEnd(Result);
    }

    public void Summary() {
        log.info("");
        log.info("+================================================================");
        log.info("Tests performed : " + (successCount + failedCount));
        log.info("Passed          : " + successCount);
        log.info("Failed          : " + failedCount);
        log.info("+================================================================");
    }

    public boolean Connect() {
        TestBegin("ConnectTo()");
        clientS7.SetConnectionType(S7.OP);
        int Result = clientS7.ConnectTo(appOptions.getIpHost(), appOptions.getIntRack(), appOptions.getIntSlot());
        if (Result == 0) {
            log.info("Connected to   : " + appOptions.getIpHost() + " (Rack=" + appOptions.getIntRack() + ", Slot=" + appOptions.getIntSlot() + ")");
            log.info("PDU negotiated : " + clientS7.PDULength() + " bytes");
        }
        TestEnd(Result);
        return Result == 0;
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
        BlockInfo(S7.Block_SFC, 1); // Get SFC 1 info (always present in a CPU)
        DBPlay();
        Summary();
    }


    public static void main(String[] args) {
        log.info(APP_LOG_PREFIX + "================ Запуск (Новая версия V2) ================. Аргументы = " + Arrays.toString(args));
        AppOptions appOptions = new AppOptions();
        try {
            appOptions.load();
        } catch (AppException e) {
            log.error("Ошибка чтения конфига", e);
            return;
        }
        DatanaSimensTestMoka7 app = new DatanaSimensTestMoka7(appOptions);
        app.run();

        log.info(APP_LOG_PREFIX + "********* Завершение программы (Версия Мока7) *********");
    }

    public void run() {
        try {
            successCount = 0;
            failedCount = 0;


            if (Connect()) {
                if (appOptions.getAppWorkMode() == EnumAppWorkMode.TEST)
                    performTests();
                else if (appOptions.getAppWorkMode() == EnumAppWorkMode.READ)
                    danataReadTest();
                else
                    log.error(ERROR_LOG_PREFIX + "Не определен режим работы: " + appOptions.getAppWorkMode() + "'");


            }


        } catch (Exception e) {
            log.error(ERROR_LOG_PREFIX + "Аварийное завершение программы: ", e);
        }

        log.info(APP_LOG_PREFIX + "********* Завершение программы (Версия Мока7) *********");
    }

    private void danataReadTest() {

        TestBegin("danataReadTest()");
        boolean isSuccess = readDataS7();
        if (isSuccess) {
            log.info("[Чтение данных] Упешно прочитано");
        }
        TestEnd(isSuccess ? 0 : 1);
    }

}
