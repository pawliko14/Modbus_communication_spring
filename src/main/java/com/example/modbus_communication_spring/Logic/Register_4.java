package com.example.modbus_communication_spring.Logic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Register_4 {
    ACTIVE_ENERGY_IMPORT_TARIFF_1(801),
    ACTIVE_ENERGY_IMPORT_TARIFF_2(805),
    ACTIVE_ENERGY_EXPORT_TARIFF_1(809),
    ACTIVE_ENERGY_EXPORT_TARIFF_2(813);

    private final int value;

    Register_4(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    public static List<String> getNames() {
        return Arrays.stream(Register_4.values()).map(Enum::name)
                .collect(Collectors.toList());
    }
}
