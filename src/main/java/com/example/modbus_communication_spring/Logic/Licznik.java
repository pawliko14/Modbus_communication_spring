package com.example.modbus_communication_spring.Logic;

import com.example.modbus_communication_spring.InfluxDBConnector.InfluxInitalizer;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

public class Licznik extends Thread {

    private static final Logger log = getLogger(Licznik.class);

    private String IP;
    private int CzestotliwoscPomiarow;
    public ModbusClient modbusClient;
    private InfluxInitalizer influxInitalizer;


    public Licznik(String ip, int czestotliowsc, InfluxInitalizer influxInitalizer) {
        this.IP = ip;
        this.CzestotliwoscPomiarow = czestotliowsc;
        this.influxInitalizer = influxInitalizer;

        modbusClient = new ModbusClient(this.GetIP(), 502);


    }

    public String GetIP() {
        return IP;
    }

    public int GetCzestotliwosc() {
        return CzestotliwoscPomiarow;
    }


    public void ConnectToLicznik() throws IOException, InterruptedException {
        try {
            modbusClient.setConnectionTimeout(10000);
            modbusClient.Connect();
            log.info("connection timout: " + modbusClient.getConnectionTimeout());
            if (modbusClient.isConnected()) {
                log.info("Connection is set as: " + GetIP());
            }
        } catch (Exception e) {
            log.error("Connection lost, trying to connect");
        }
    }


    public void StartGenerateDataSet() throws IOException, ModbusException, InterruptedException {
        if (modbusClient.isConnected()) {
            log.info("connected to Sentron");
            while (true) {
                for (Register_2 register2 : Register_2.values()) {
                    this.insertIntoInflux(ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(register2.getValue(), 2), ModbusClient.RegisterOrder.HighLow), register2.name());
                }
                for (Register_4 register4 : Register_4.values()) {
                    this.insertIntoInflux(ModbusClient.ConvertRegistersToDouble(modbusClient.ReadHoldingRegisters(register4.getValue(), 4), ModbusClient.RegisterOrder.HighLow), register4.name());
                }
                Thread.sleep(this.GetCzestotliwosc());
            }
        } else {
            log.error("Connection lost");
            modbusClient.Disconnect();
        }
    }

    @Override
    public void run() {
        try {
            this.ConnectToLicznik();
            this.StartGenerateDataSet();
        } catch (IOException | InterruptedException e) {
            log.error("no connection, cause:  \n" + e);
            try {
                Thread.sleep(2000);
                run();
            } catch (InterruptedException ex) {
                log.error("Interruption occured, cause: {}", ex);
            }
        } catch (ModbusException e) {
            log.error("Modbus exception occured, cause: {}", e);
        }

    }

    public void insertIntoInflux(double value, String fieldName) {
        insertToInflux(value, influxInitalizer, "host", fieldName);
    }


    private void insertToInflux(Double value, InfluxInitalizer influxInitalizer, String host, String fieldName) {
        influxInitalizer.writeDataDouble(value, host, fieldName);
    }

}


