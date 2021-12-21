package com.example.modbus_communication_spring.Logic;

import com.example.modbus_communication_spring.InfluxDBConnector.InfluxInitalizer;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.sql.SQLException;
import java.util.List;

public class Licznik extends Thread {

    private Thread t;

    private String IP;
    private int CzestotliwoscPomiarow;
    public ModbusClient modbusClient;
    private InfluxInitalizer influxInitalizer;
    private List<Register> registers;


    public Licznik(String ip, int czestotliowsc, List<Register> registers, InfluxInitalizer influxInitalizer) {
        this.IP = ip;
        this.CzestotliwoscPomiarow = czestotliowsc;
        this.registers = registers;
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
            System.out.println("timeout: " + modbusClient.getConnectionTimeout());
            if (modbusClient.isConnected()) {
                System.out.println("Connection is set as: " + GetIP());
            }
        } catch (Exception e) {
            System.out.println("Connection lost, trying to connect:");
        }
    }


    public void StartGenerateDataSet() throws IllegalArgumentException, ModbusException, IOException, InterruptedException, SQLException {

        System.out.println("checking");
        if (modbusClient.isConnected()) {
            System.out.println("connected ");

            while (true) {
                for (Register register : registers) {
                    this.insertIntoInflux(ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(register.getValue(), 2), ModbusClient.RegisterOrder.HighLow)
                            , register.name());
                }
                CheckMemoryUsage();
                Thread.sleep(this.GetCzestotliwosc());
            }
        } else {
            System.out.println("Connection lost");
            modbusClient.Disconnect();
        }
    }

    private void CheckMemoryUsage() {
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        heapMemoryUsage.getUsed();
        System.out.println("HeapMemoryUsage: " + heapMemoryUsage.getUsed());
    }

    public void run() {
        try {
            this.ConnectToLicznik();
            this.StartGenerateDataSet();
        } catch (SQLException | IOException | InterruptedException e) {
            System.out.println("no connection here - trying to reconnect: ");
            System.out.println("problem :" + e);
            try {
                Thread.sleep(2000);
                run();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } catch (ModbusException e) {
            e.printStackTrace();
        }

    }

    public void insertIntoInflux(double value, String fieldName) {
        insertToInflux(value, influxInitalizer, "host1", fieldName);
        System.out.println("Added : " + value);
    }


    private void insertToInflux(Double value, InfluxInitalizer influxInitalizer, String host1, String fieldName) {
        influxInitalizer.writeDataDouble(value, host1, fieldName);
    }

}


