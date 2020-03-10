package ru.datana.siemensopc.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public enum EnumSiemensDataType {
    TYPE_BYTE(8, false),
    TYPE_BIT(1, false),
    TYPE_UNSIGNED_WORD(16, false),
    TYPE_UNSIGNED_DOUBLE_WORD(32, false),
    TYPE_REAL(32, true);

    @Getter
    private final int bitCount;

    @Getter
    private final boolean isSigned;
}
