package application;

import jssc.SerialPort;
import jssc.SerialPortException;
//import jssc.SerialPortTimeoutException;

public class Test {

    public static void main(String[] args){
        try {
            SerialPort serial = new SerialPort("COM3");
            serial.openPort();
            serial.writeString("$FW;\r");
            String res = serial.readString(10);
            System.out.println(res);
        }
        catch (SerialPortException ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println("OK");

    }
}
