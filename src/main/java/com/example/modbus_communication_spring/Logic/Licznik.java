package com.example.modbus_communication_spring.Logic;

import com.example.modbus_communication_spring.InfluxDBConnector.InfluxInitalizer;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Licznik extends Thread {

    private Thread t;

    private String IP;
    private int CzestotliwoscPomiarow;
    public ModbusClient modbusClient;
    private InfluxInitalizer influxInitalizer;
    private List<Register> registers_2;
    private List<Register> registers_4;
    private static short GLOBAL_COUNTER = 0;

    public Licznik(String ip, int czestotliowsc, InfluxInitalizer influxInitalizer) {
        this.IP = ip;
        this.CzestotliwoscPomiarow = czestotliowsc;
        this.influxInitalizer = influxInitalizer;

        modbusClient = new ModbusClient(this.GetIP(), 502);

        this.registers_2 = new ArrayList<>();
        this.registers_4 = new ArrayList<>();

        fillRegister_2();
        fillRegister_4();
    }

    private void fillRegister_4() {
        registers_4.add(Register.ACTIVE_ENERGY_IMPORT_TARIFF_1);
        registers_4.add(Register.ACTIVE_ENERGY_IMPORT_TARIFF_2);
        registers_4.add(Register.ACTIVE_ENERGY_EXPORT_TARIFF_1);
        registers_4.add(Register.ACTIVE_ENERGY_EXPORT_TARIFF_2);

    }

    private void fillRegister_2() {
        registers_2.add(Register.FREQUENCY);
        registers_2.add(Register.TOTAL_APPARENT_POWER);
        registers_2.add(Register.TOTAL_ACTIVE_POWER);
        registers_2.add(Register.TOTAL_REACTIVE_POWER);
        registers_2.add(Register.WORKING_HOURS);
        registers_2.add(Register.VOLTAGE_A_B);
        registers_2.add(Register.VOLTAGE_B_C);
        registers_2.add(Register.VOLTAGE_C_A);
        registers_2.add(Register.CURRENT_A);
        registers_2.add(Register.CURRENT_B);
        registers_2.add(Register.CURRENT_C);
        registers_2.add(Register.AVERAGE_CURRENT);
        registers_2.add(Register.DEMAND_ACTIVE_POWER_IMPORT);
        registers_2.add(Register.DEMAND_ACTIVE_POWER_EXPORT);
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


    public void StartGenerateDataSet() throws IllegalArgumentException, ModbusException, IOException, InterruptedException, SQLException {


        if (modbusClient.isConnected()) {
            log.info("connected to Sentron");

            while (true) {
                for (Register register : registers_2) {
                    this.insertIntoInflux(ModbusClient.ConvertRegistersToFloat(modbusClient.ReadHoldingRegisters(register.getValue(), 2), ModbusClient.RegisterOrder.HighLow)
                            , register.name());
                }
                for (Register register : registers_4) {
                    this.insertIntoInflux(ModbusClient.ConvertRegistersToDouble(modbusClient.ReadHoldingRegisters(register.getValue(), 4), ModbusClient.RegisterOrder.HighLow)
                            , register.name());
                }
                CheckMemoryUsage();
                Thread.sleep(this.GetCzestotliwosc());
                GLOBAL_COUNTER++;
            }
        } else {
            log.error("Connection lost");
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
            log.error("no connection, cause:  \n" + e);
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
        insertToInflux(value, influxInitalizer, "host", fieldName);
     //   if(GLOBAL_COUNTER == 10) {
            log.info("Added: " + value + " to field: " + fieldName);
       // }
    }


    private void insertToInflux(Double value, InfluxInitalizer influxInitalizer, String host, String fieldName) {
        influxInitalizer.writeDataDouble(value, host, fieldName);
    }

}


