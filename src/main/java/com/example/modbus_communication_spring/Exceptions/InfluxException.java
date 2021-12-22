package com.example.modbus_communication_spring.Exceptions;

import javax.annotation.Nullable;

public class InfluxException extends com.influxdb.exceptions.InfluxException {
    public InfluxException(@Nullable String message) {
        super(message);
    }
}
