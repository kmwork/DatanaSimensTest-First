package ru.datana.siemensopc.moka.contants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public enum EnumConnectionMoka7Type {

    // Connection types
    PG((short) 0x010),
    OP((short) 0x02),
    S7_BASIC((short) 0x03);

    @Getter
    private final short code;
}
