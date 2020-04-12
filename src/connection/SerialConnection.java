package connection;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortEvent;

public class SerialConnection {

    private SerialPort port;
    private byte[] eol = null;

    public SerialConnection(SerialPort port) throws ConnectionException {
        this.port = port;
        try {
            if (!this.port.isOpened()) {
                this.port.openPort();
            }
            this.port.setFlowControlMode(
                            SerialPort.FLOWCONTROL_RTSCTS_IN |
                            SerialPort.FLOWCONTROL_RTSCTS_OUT);
        } catch (SerialPortException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    public SerialConnection(String portName, int baudrate, int data, int stop, int parity) throws ConnectionException {
        this.port = new SerialPort(portName);
        try {
            this.port.openPort();
            this.port.setParams(baudrate, data, stop, parity);
            this.port.setFlowControlMode(
                            SerialPort.FLOWCONTROL_RTSCTS_IN |
                            SerialPort.FLOWCONTROL_RTSCTS_OUT);
        } catch (SerialPortException ex) {
            throw new ConnectionException(ex.getMessage());
        }

    }

    public void setSerialParams(int baudrate, int data, int stop, int parity) throws ConnectionException {
        if(this.port == null) {
            throw new ConnectionException("Unknown com");
        }
        try {
            if (!this.port.isOpened()) {
                this.port.openPort();
            }
            this.port.setParams(baudrate, data, stop, parity);
        } catch (SerialPortException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    public void setEOL(byte[] eol) {
        if(eol == null) {
            this.eol = null;
            return;
        }
        this.eol = new byte[eol.length];
        System.arraycopy(eol, 0, this.eol, 0, eol.length);
    }

    public void writeLine(String line) throws  ConnectionException {
        boolean res = false;
        try {
            if(this.eol == null) {
                res = this.port.writeString(line);
            } else {
                res = this.port.writeString(line + new String(this.eol));
            }
        } catch (SerialPortException ex) {
            throw new ConnectionException(ex.getMessage());
        }
        if(!res) {
            throw new ConnectionException("Error writing to port");
        }
    }

    public byte[] readBytes(int size) throws ConnectionException {
        byte[] res;
        try {
            res = this.port.readBytes(size);
        } catch (SerialPortException ex) {
//            port.g
            throw new ConnectionException(ex.getMessage());
        }
        return res;
    }

    public void addListener(SerialPortEventListener listener) throws ConnectionException {
        try {
            this.port.addEventListener(listener, SerialPort.MASK_RXCHAR);
        } catch (SerialPortException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    public void close() throws ConnectionException {
        boolean res;
        try {
            res = this.port.closePort();
        } catch (SerialPortException ex) {
            throw new ConnectionException(ex.getMessage());
        }
        if(!res) {
            throw new ConnectionException("Error close port");
        }

    }
}
