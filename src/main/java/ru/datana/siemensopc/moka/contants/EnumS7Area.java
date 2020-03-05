package ru.datana.siemensopc.moka.contants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum EnumS7Area {
    S7AreaPE(0x81),
    S7AreaPA(0x82),
    S7AreaMK(0x83),
    S7AreaDB(0x84),
    S7AreaCT(0x1C),
    S7AreaTM(0x1D);

    private final int s7AreaCode;
}
