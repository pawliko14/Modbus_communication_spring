package com.example.modbus_communication_spring.Logic;

public enum Register {
    FREQUENCY(55),
    TOTAL_APPARENT_POWER(63),
    TOTAL_ACTIVE_POWER(65),
    TOTAL_REACTIVE_POWER(67),
    WORKING_HOURS(213),
    VOLTAGE_A_B(7),
    VOLTAGE_B_C(9),
    VOLTAGE_C_A(11),
    CURRENT_A(13),
    CURRENT_B(15),
    CURRENT_C(17),
    AVERAGE_CURRENT(61);

    private final int value;

    Register(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
