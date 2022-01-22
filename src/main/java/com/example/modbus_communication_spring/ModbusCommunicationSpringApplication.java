package com.example.modbus_communication_spring;

import com.example.modbus_communication_spring.InfluxDBConnector.InfluxInitalizer;
import com.example.modbus_communication_spring.Logic.Licznik;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ModbusCommunicationSpringApplication implements CommandLineRunner {

    private static Licznik l1;


    @Value("${sentron.IP}")
    private String IPADDRESS;
    @Value("${sentron.saveFreqency}")
    private int FREQUENCY;
    @Value("${influx.token}")
    private String token;
    @Value("${influx.bucket}")
    private String bucket;
    @Value("${influx.orgName}")
    private String org;
    @Value("${influx.connection}")
    private String connection;
    @Value("${influx.measurmentName}")
    private String measurmentName;
    @Value("${influx.measurmentTag}")
    private String measurmentTag;
    @Value("${sentron.decimalFormat}")
    private String decimalFormat;

    private static InfluxInitalizer influxInitalizer;

    public static void main(String[] args) {
        SpringApplication.run(ModbusCommunicationSpringApplication.class, args);
    }

    @Override
    public void run(String... args) {
        influxInitalizer = new InfluxInitalizer(token, bucket, org, connection, measurmentName, measurmentTag, decimalFormat);

        l1 = new Licznik(IPADDRESS,
                FREQUENCY,
                influxInitalizer
        );
        l1.start();

    }
}
