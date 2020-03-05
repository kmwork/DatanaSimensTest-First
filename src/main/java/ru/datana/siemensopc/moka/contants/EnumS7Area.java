package ru.datana.siemensopc.moka.contants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum EnumS7Area {
    S7AreaPE(0x81, "Process Inputs"),
    S7AreaPA(0x82, "Process Outputs"),
    S7AreaMK(0x83, "Merkers"),
    S7AreaDB(0x84, "DB"),
    S7AreaCT(0x1C, "Counters"),
    S7AreaTM(0x1D, "Timers");

    private final int s7AreaCode;
    private final String userDesc;


}
