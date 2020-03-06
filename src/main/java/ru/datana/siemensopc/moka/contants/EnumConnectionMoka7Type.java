package ru.datana.siemensopc.moka.contants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public enum EnumConnectionMoka7Type {

    // Connection types
    PG(0x010),
    OP(0x02),
    S7_BASIC(0x03);

    @Getter
    private final int code;
}
