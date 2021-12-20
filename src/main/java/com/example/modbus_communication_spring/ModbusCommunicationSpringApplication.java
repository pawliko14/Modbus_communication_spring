package com.example.modbus_communication_spring;

import com.example.modbus_communication_spring.InfluxDBConnector.InfluxInitalizer;
import com.example.modbus_communication_spring.Logic.Licznik;
import com.example.modbus_communication_spring.Logic.Register;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;

//@SpringBootApplication(scanBasePackages={"com.example.modbus_communication_spring"})
@SpringBootApplication
public class ModbusCommunicationSpringApplication implements CommandLineRunner {

    @Value("${sentron.IP}")
    private String IPADDRESS;

    @Value("${sentron.saveFreqency}")
    private int FREQUENCY;
    private static Licznik l1;

    @Value("${influx.token}")
    private  String token;
    @Value("${influx.bucket}")
    private  String bucket;
    @Value("${influx.orgName}")
    private  String org;
    @Value("${influx.connection}")
    private  String connection;

    @Value("${influx.measurmentName}")
    private  String measurmentName;
    @Value("${influx.measurmentTag}")
    private String measurmentTag;

    private static InfluxInitalizer influxInitalizer;

    public static void main(String[] args) {
        SpringApplication.run(ModbusCommunicationSpringApplication.class, args);
    }

    @Override
    public void run(String... args) {
        influxInitalizer = new InfluxInitalizer(token, bucket, org, connection, measurmentName, measurmentTag);

        List<Register> registers = Arrays.asList(Register.values());

        l1 = new Licznik(IPADDRESS,
                FREQUENCY,
                registers,
                influxInitalizer
        );
        l1.start();

    }
}
