package com.example.modbus_communication_spring.InfluxDBConnector;


import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

import java.time.Instant;
import java.util.List;


public class InfluxInitalizer {

    private final InfluxDBClient client;

    private String token;
    private String bucket;
    private String org;
    private String connection;
    private String measurmentName;
    private String measurmentTag;

    public InfluxInitalizer(String token, String bucket, String org, String connection, String measurmentName, String measurmentTag) {
        this.token = token;
        this.bucket = bucket;
        this.org = org;
        this.connection = connection;
        this.measurmentName = measurmentName;
        this.measurmentTag = measurmentTag;

        this.client = InfluxDBClientFactory.create(this.connection, this.token.toCharArray());
    }

    public void writeDataDouble(Double value, String host, String fieldName) {
        Point point = Point
                .measurement(measurmentName)
                .addTag(measurmentTag, host)
                .addField(fieldName, value)
                .time(Instant.now(), WritePrecision.NS);

        try (WriteApi writeApi = client.getWriteApi()) {
            writeApi.writePoint(bucket, org, point);
        }
        String query = "from(bucket: \"" + bucket + "\") |> range(start: -1h)";
        List<FluxTable> tables = client.getQueryApi().query(query, org);
    }


    @Measurement(name = "Sentron")
    public static class Sentron {
        @Column(tag = true)
        String host;
        @Column
        Double doubleData;
        @Column(timestamp = true)
        Instant time;
    }

}