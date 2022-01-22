package com.example.modbus_communication_spring.InfluxDBConnector;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.text.DecimalFormat;
import java.time.Instant;

public class InfluxInitalizer {

    private final InfluxDBClient client;
    private DecimalFormat numberFormat;

    private String token;
    private String bucket;
    private String org;
    private String connection;
    private String measurmentName;
    private String measurmentTag;


    public InfluxInitalizer(String token, String bucket, String org, String connection, String measurmentName, String measurmentTag, String decimalFormat) {
        this.token = token;
        this.bucket = bucket;
        this.org = org;
        this.connection = connection;
        this.measurmentName = measurmentName;
        this.measurmentTag = measurmentTag;


        this.numberFormat = new DecimalFormat(decimalFormat);
        this.client = InfluxDBClientFactory.create(this.connection, this.token.toCharArray());
    }

    public void writeDataDouble(Double value, String host, String fieldName) {
        Point point = Point
                .measurement(measurmentName)
                .addTag(measurmentTag, host)
                .addField(fieldName, Double.parseDouble(this.numberFormat.format(value)))
                .addField(fieldName, value)
                .time(Instant.now(), WritePrecision.NS);

        try (WriteApi writeApi = client.getWriteApi()) {
            writeApi.writePoint(bucket, org, point);
        }
    }
}