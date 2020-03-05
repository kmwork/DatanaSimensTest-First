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
package ru.datana.siemensopc.executer;

/**
 * @author Dave Nardella
 */

import Moka7.*;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.config.AppConts;
import ru.datana.siemensopc.config.AppOptions;
import ru.datana.siemensopc.config.EnumAppWorkMode;
import ru.datana.siemensopc.utils.AppException;
import ru.datana.siemensopc.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class Moka7Executer implements IExecutor {


    private long Elapsed;
    private byte[] Buffer = new byte[65536]; // 64K buffer (maximum for S7400 systems)
    private final S7Client clientS7 = new S7Client();
    private int successCount = 0;
    private int failedCount = 0;
    private int DataToMove; // Data size to read/write
    private int CurrentStatus = S7.S7CpuStatusUnknown;

    private final AppOptions appOptions;

    public Moka7Executer(AppOptions appOptions) {
        this.appOptions = appOptions;
    }

    private String prefixMethod(String functionName) {
        return "[Method: " + functionName + "] ";
    }

    private void TestBegin(String functionName) {
        String prefix = prefixMethod(functionName);
        log.info(prefix);
        log.info(prefix + "+================================================================");
        Elapsed = System.currentTimeMillis();
    }

    private void TestEnd(String functionName, int result) {
        String prefix = prefixMethod(functionName);
        if (result != 0) {
            failedCount++;
            Error(functionName, result);
        } else
            successCount++;
        log.info(prefix + " Время выполнения = " + (System.currentTimeMillis() - Elapsed) + " ms");
        log.info(prefix + "-------------------------------------------------------------");
    }

    private void Error(String functionName, int erorrCode) {
        String prefix = prefixMethod(functionName) + "[Error] ";
        log.error(prefix + " Код ошибки = " + erorrCode + ". Описание: " + S7Client.ErrorText(erorrCode));
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
        IntByRef SizeRead = new IntByRef(0);

        String method = "Чтение данных";
        TestBegin(method);
        int result = -1;
        try {
            clientS7.DBGet(appOptions.getIntS7DBNumber(), Buffer, SizeRead);
        } finally {
            TestEnd(method, result);
        }

        if (result == 0) {
            DataToMove = SizeRead.Value; // Stores DB size for next test
            log.info("DB " + appOptions.getIntS7DBNumber() + " - Size read " + DataToMove + " bytes");
            FormatUtils.hexDump(Buffer, DataToMove);
            return true;
        }
        return false;
    }

    private void DBRead() {
        String method = "чтение блока";
        TestBegin(method);
        int result = -1;
        try {
            clientS7.ReadArea(appOptions.getEnumMoka7AreaType().getS7AreaCode(), appOptions.getIntS7DBNumber(), appOptions.getIntOffset(), DataToMove, Buffer);
        } finally {
            TestEnd(method, result);
        }
        if (result == 0) {
            log.info(AppConts.SUCCESS_LOG_PREFIX + "Прочитано из getEnumS7Area = " + appOptions.getEnumMoka7AreaType().getUserDesc());
        }
        TestEnd(method, result);
    }

    private void DBWrite() {
        String method = "записиь данных";
        TestBegin(method);
        int result = -1;
        try {
            result = clientS7.WriteArea(appOptions.getEnumMoka7AreaType().getS7AreaCode(), appOptions.getIntS7DBNumber(), appOptions.getIntOffset(), DataToMove, Buffer);
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
                    stringBuilder.append(("STOP");
                    break;
                default:
                    stringBuilder.append("Unknown (" + PlcStatus.Value + ")");
            }
            log.info(prefix + " Получен статус = " + stringBuilder);
        }
        CurrentStatus = PlcStatus.Value;

    }

    public void DoRun() {
        TestBegin("PlcHotStart()");
        int result = clientS7.PlcHotStart();
        if (result == 0)
            log.info("PLC Started");
        TestEnd(method, result);
    }

    public void DoStop() {
        TestBegin("PlcStop()");
        int result = clientS7.PlcStop();
        if (result == 0)
            log.info("PLC Stopped");
        TestEnd(method, result);
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
        int result;
        TestBegin("GetOrderCode()");
        S7OrderCode OrderCode = new S7OrderCode();
        result = clientS7.GetOrderCode(OrderCode);
        if (result == 0) {
            log.info("Order Code        : " + OrderCode.Code());
            log.info("Firmware version  : " + OrderCode.V1 + "." + OrderCode.V2 + "." + OrderCode.V3);
        }
        TestEnd(method, result);

        TestBegin("GetCpuInfo()");
        S7CpuInfo CpuInfo = new S7CpuInfo();
        result = clientS7.GetCpuInfo(CpuInfo);
        if (result == 0) {
            log.info("Module Type Name  : " + CpuInfo.ModuleTypeName());
            log.info("Serial Number     : " + CpuInfo.SerialNumber());
            log.info("AS Name           : " + CpuInfo.ASName());
            log.info("CopyRight         : " + CpuInfo.Copyright());
            log.info("Module Name       : " + CpuInfo.ModuleName());
        }
        TestEnd(method, result);

        TestBegin("GetCpInfo()");
        S7CpInfo CpInfo = new S7CpInfo();
        result = clientS7.GetCpInfo(CpInfo);
        if (result == 0) {
            log.info("Max PDU Length    : " + CpInfo.MaxPduLength);
            log.info("Max connections   : " + CpInfo.MaxConnections);
            log.info("Max MPI rate (bps): " + CpInfo.MaxMpiRate);
            log.info("Max Bus rate (bps): " + CpInfo.MaxBusRate);
        }
        TestEnd(method, result);
    }

    public void GetDateAndTime() {
        Date PlcDateTime = new Date();
        TestBegin("GetPlcDateTime()");
        int result = clientS7.GetPlcDateTime(PlcDateTime);
        if (result == 0)
            log.info("CPU Date/Time : " + PlcDateTime);
        TestEnd(method, result);
    }

    public void SyncDateAndTime() {
        TestBegin("SetPlcSystemDateTime()");
        int result = clientS7.SetPlcSystemDateTime();
        TestEnd(method, result);
    }

    public void ReadSzl() {
        S7Szl SZL = new S7Szl(1024);
        TestBegin("ReadSZL() - ID : 0x0011, IDX : 0x0000");
        int result = clientS7.ReadSZL(0x0011, 0x0000, SZL);
        if (result == 0) {
            log.info("LENTHDR : " + SZL.LENTHDR);
            log.info("N_DR    : " + SZL.N_DR);
            log.info("Size    : " + SZL.DataSize);
            FormatUtils.hexDump(SZL.Data, SZL.DataSize);
        }
        TestEnd(method, result);
    }

    public void GetProtectionScheme() {
        S7Protection Protection = new S7Protection();
        TestBegin("GetProtection()");
        int result = clientS7.GetProtection(Protection);
        if (result == 0) {
            log.info("sch_schal : " + Protection.sch_schal);
            log.info("sch_par   : " + Protection.sch_par);
            log.info("sch_rel   : " + Protection.sch_rel);
            log.info("bart_sch  : " + Protection.bart_sch);
            log.info("anl_sch   : " + Protection.anl_sch);
        }
        TestEnd(method, result);
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
        TestBegin("connectMoka7()");
        clientS7.SetConnectionType(S7.OP);
        int result = clientS7.ConnectTo(appOptions.getIpHost(), appOptions.getIntRack(), appOptions.getIntSlot());
        if (result == 0) {
            log.info("Connected to   : " + appOptions.getIpHost() + " (Rack=" + appOptions.getIntRack() + ", Slot=" + appOptions.getIntSlot() + ")");
            log.info("PDU negotiated : " + clientS7.PDULength() + " bytes");
        }
        TestEnd(method, result);
        return result == 0;
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

    @Override
    public void run() throws AppException {
        try {
            successCount = 0;
            failedCount = 0;


            if (connectMoka7()) {
                if (appOptions.getAppWorkMode() == EnumAppWorkMode.TEST)
                    performTests();
                else if (appOptions.getAppWorkMode() == EnumAppWorkMode.READ)
                    danataReadTest();
                else
                    log.error(AppConts.ERROR_LOG_PREFIX + "Не определен режим работы: " + appOptions.getAppWorkMode() + "'");


            }


        } catch (Exception e) {
            log.error(AppConts.ERROR_LOG_PREFIX + "Аварийное завершение программы: ", e);
        }
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
