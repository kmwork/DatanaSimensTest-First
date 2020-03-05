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
import com.github.s7connector.api.DaveArea;
import lombok.extern.slf4j.Slf4j;
import ru.datana.siemensopc.utils.FormatUtils;
import ru.datana.siemensopc.utils.ValueParser;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

@Slf4j
public class DatanaSimensTestMoka7 {
    private static final String CONF_FILE_NAME = "datana_siemens.properties";
    private static final String SYS_DIR_PROP = "app.dir";
    private static final String ENCODING_PROP = "file.encoding";

    // If MakeAllTests = true, also DBWrite and Run/Stop tests will be performed
    private static final boolean MakeAllTests = true;

    private static long Elapsed;
    private static byte[] Buffer = new byte[65536]; // 64K buffer (maximum for S7400 systems)
    private static final S7Client Client = new S7Client();
    private static int ok = 0;
    private static int ko = 0;
    private static String IpAddress = "";
    private static int Rack = 0; // Default 0 for S7300
    private static int Slot = 2; // Default 2 for S7300 
    private static int DBSample = 1; // Sample DB that must be present in the CPU
    private static int DataToMove; // Data size to read/write
    private static int CurrentStatus = S7.S7CpuStatusUnknown;



    static void TestBegin(String FunctionName) {
        log.info("");
        log.info("+================================================================");
        log.info("| " + FunctionName);
        log.info("+================================================================");
        Elapsed = System.currentTimeMillis();
    }

    static void TestEnd(int Result) {
        if (Result != 0) {
            ko++;
            Error(Result);
        } else
            ok++;
        log.info("Execution time " + (System.currentTimeMillis() - Elapsed) + " ms");
    }

    static void Error(int Code) {
        log.info(S7Client.ErrorText(Code));
    }

    static void BlockInfo(int BlockType, int BlockNumber) {
        S7BlockInfo Block = new S7BlockInfo();
        TestBegin("GetAgBlockInfo()");

        int Result = Client.GetAgBlockInfo(BlockType, BlockNumber, Block);
        if (Result == 0) {
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
        }
        TestEnd(Result);
    }

    public static boolean DBGet() {
        IntByRef SizeRead = new IntByRef(0);
        TestBegin("DBGet()");
        int Result = Client.DBGet(DBSample, Buffer, SizeRead);
        TestEnd(Result);
        if (Result == 0) {
            DataToMove = SizeRead.Value; // Stores DB size for next test
            log.info("DB " + DBSample + " - Size read " + DataToMove + " bytes");
            FormatUtils.hexDump(Buffer, DataToMove);
            return true;
        }
        return false;
    }

    public static void DBRead() {
        TestBegin("ReadArea()");
        int Result = Client.ReadArea(S7.S7AreaDB, DBSample, 0, DataToMove, Buffer);
        if (Result == 0) {
            log.info("DB " + DBSample + " succesfully read using size reported by DBGet()");
        }
        TestEnd(Result);
    }

    public static void DBWrite() {
        TestBegin("WriteArea()");
        int Result = Client.WriteArea(S7.S7AreaDB, DBSample, 0, DataToMove, Buffer);
        if (Result == 0) {
            log.info("DB " + DBSample + " succesfully written using size reported by DBGet()");
        }
        TestEnd(Result);
    }

    /**
     * Performs read and write on a given DB
     */
    public static void DBPlay() {
        // We use DBSample (default = DB 1) as DB Number
        // modify it if it doesn't exists into the CPU.
        if (DBGet()) {
            DBRead();
            if (MakeAllTests)
                DBWrite();
        }
    }

    public static void Delay(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
        }
    }

    public static void ShowStatus() {
        IntByRef PlcStatus = new IntByRef(S7.S7CpuStatusUnknown);
        TestBegin("GetPlcStatus()");
        int Result = Client.GetPlcStatus(PlcStatus);
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

    public static void DoRun() {
        TestBegin("PlcHotStart()");
        int Result = Client.PlcHotStart();
        if (Result == 0)
            log.info("PLC Started");
        TestEnd(Result);
    }

    public static void DoStop() {
        TestBegin("PlcStop()");
        int Result = Client.PlcStop();
        if (Result == 0)
            log.info("PLC Stopped");
        TestEnd(Result);
    }

    public static void RunStop() {
        switch (CurrentStatus) {
            case S7.S7CpuStatusRun:
                DoStop();
                Delay(1000);
                DoRun();
                break;
            case S7.S7CpuStatusStop:
                DoRun();
                Delay(1000);
                DoStop();
        }
    }

    public static void GetSysInfo() {
        int Result;
        TestBegin("GetOrderCode()");
        S7OrderCode OrderCode = new S7OrderCode();
        Result = Client.GetOrderCode(OrderCode);
        if (Result == 0) {
            log.info("Order Code        : " + OrderCode.Code());
            log.info("Firmware version  : " + OrderCode.V1 + "." + OrderCode.V2 + "." + OrderCode.V3);
        }
        TestEnd(Result);

        TestBegin("GetCpuInfo()");
        S7CpuInfo CpuInfo = new S7CpuInfo();
        Result = Client.GetCpuInfo(CpuInfo);
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
        Result = Client.GetCpInfo(CpInfo);
        if (Result == 0) {
            log.info("Max PDU Length    : " + CpInfo.MaxPduLength);
            log.info("Max connections   : " + CpInfo.MaxConnections);
            log.info("Max MPI rate (bps): " + CpInfo.MaxMpiRate);
            log.info("Max Bus rate (bps): " + CpInfo.MaxBusRate);
        }
        TestEnd(Result);
    }

    public static void GetDateAndTime() {
        Date PlcDateTime = new Date();
        TestBegin("GetPlcDateTime()");
        int Result = Client.GetPlcDateTime(PlcDateTime);
        if (Result == 0)
            log.info("CPU Date/Time : " + PlcDateTime);
        TestEnd(Result);
    }

    public static void SyncDateAndTime() {
        TestBegin("SetPlcSystemDateTime()");
        int Result = Client.SetPlcSystemDateTime();
        TestEnd(Result);
    }

    public static void ReadSzl() {
        S7Szl SZL = new S7Szl(1024);
        TestBegin("ReadSZL() - ID : 0x0011, IDX : 0x0000");
        int Result = Client.ReadSZL(0x0011, 0x0000, SZL);
        if (Result == 0) {
            log.info("LENTHDR : " + SZL.LENTHDR);
            log.info("N_DR    : " + SZL.N_DR);
            log.info("Size    : " + SZL.DataSize);
            FormatUtils.hexDump(SZL.Data, SZL.DataSize);
        }
        TestEnd(Result);
    }

    public static void GetProtectionScheme() {
        S7Protection Protection = new S7Protection();
        TestBegin("GetProtection()");
        int Result = Client.GetProtection(Protection);
        if (Result == 0) {
            log.info("sch_schal : " + Protection.sch_schal);
            log.info("sch_par   : " + Protection.sch_par);
            log.info("sch_rel   : " + Protection.sch_rel);
            log.info("bart_sch  : " + Protection.bart_sch);
            log.info("anl_sch   : " + Protection.anl_sch);
        }
        TestEnd(Result);
    }

    public static void Summary() {
        log.info("");
        log.info("+================================================================");
        log.info("Tests performed : " + (ok + ko));
        log.info("Passed          : " + ok);
        log.info("Failed          : " + ko);
        log.info("+================================================================");
    }

    public static boolean Connect() {
        TestBegin("ConnectTo()");
        Client.SetConnectionType(S7.OP);
        int Result = Client.ConnectTo(IpAddress, Rack, Slot);
        if (Result == 0) {
            log.info("Connected to   : " + IpAddress + " (Rack=" + Rack + ", Slot=" + Slot + ")");
            log.info("PDU negotiated : " + Client.PDULength() + " bytes");
        }
        TestEnd(Result);
        return Result == 0;
    }


    public static void PerformTests() {
        GetSysInfo();
        GetProtectionScheme();
        GetDateAndTime();
        if (MakeAllTests)
            SyncDateAndTime();
        ReadSzl();
        ShowStatus();
        if (MakeAllTests)
            RunStop();
        BlockInfo(S7.Block_SFC, 1); // Get SFC 1 info (always present in a CPU)
        DBPlay();
        Summary();
    }

    public static void main(String[] args) throws IOException {
        log.info("[DatanaSimensTestMoka7] ================ Запуск (Новая версия V2) ================. Аргументы = " + Arrays.toString(args));
        try {
            String dirConf = ValueParser.readPropAsText(System.getProperties(), SYS_DIR_PROP);
            String fileEncoding = ValueParser.readPropAsText(System.getProperties(), ENCODING_PROP);

            File f = new File(dirConf, CONF_FILE_NAME);
            if (!f.isFile() || !f.exists()) {
                log.error("Файл не найден: " + f.getAbsolutePath());
                System.exit(-100);
            }

            Properties p = new Properties();
            try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), fileEncoding));) {
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
            boolean isDebug = "true".equalsIgnoreCase(ValueParser.readPropAsText(p, "debug.boolean"));

            int successCount = 0;
            int errorCount = 0;


            Rack = intRack;
            Slot = intSlot;
            IpAddress = ipHost;

            if (Connect()) {
                if (isDebug) PerformTests();
            }

        } catch (Exception e) {
            log.error("[App-Error: Аварийное завершение программы: ", e);
        }

        log.info("[DatanaSimensTestMoka7] ********* Завершение программы (Версия Мока7)*********");
    }

}
