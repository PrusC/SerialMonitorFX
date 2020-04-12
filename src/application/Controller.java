package application;

import connection.ConnectionException;
import connection.SerialConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.scene.input.KeyCode;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;


public class Controller {

    private SerialConnection connection;

    @FXML
    private Button serialNamesUpdate;

    @FXML
    private Button serialConnectBtn;

    @FXML
    private Button serialRxClear;

    @FXML
    private Button serialSend;

    @FXML
    private ChoiceBox<String> serialNamesChoice;

    @FXML
    private ChoiceBox<Integer> serialBaudrateChoice;

    @FXML
    private ChoiceBox<String> serialEndOfLine;

    @FXML
    private TextArea serialTextBoxRx;

    @FXML
    private TextField serialCmdText;

    @FXML
    public void initialize(){
        this.loadComNames();
        this.loadBaudratesList();
        this.loadEndOfLines();
        serialNamesUpdate.setOnAction(e -> {this.loadComNames();});
        serialConnectBtn.setOnAction(e -> {this.clickSerialBtnConnect();});
        serialRxClear.setOnAction(e -> {serialTextBoxRx.clear();});
        serialSend.setOnAction(e -> {sendCmd();});
        serialEndOfLine.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                        if(connection != null) {
                            connection.setEOL(constructEOFBytes(t1));
                        }
                    }
                }
        );
        serialCmdText.setOnKeyPressed(e->{
            if(e.getCode() == KeyCode.ENTER) {
                sendCmd();
            }
        });
    }

    @FXML
    private void loadComNames() {
        String[] com = SerialPortList.getPortNames();
        serialNamesChoice.setItems(FXCollections.observableArrayList(com));
    }

    private void loadBaudratesList() {
        Integer[] baudrates = new Integer[] {110, 300, 600, 1200, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000};
        serialBaudrateChoice.setItems(FXCollections.observableArrayList(baudrates));
        serialBaudrateChoice.setValue(9600);
    }

    private void loadEndOfLines() {
        String[] eol = new String[] {"NONE", "CR", "LF", "LFCR"};
        serialEndOfLine.setItems(FXCollections.observableArrayList(eol));
        serialEndOfLine.setValue("CR");
    }

    private void sendCmd() {
        String cmd = serialCmdText.getText();

        if(this.connection != null) {
            try {
                this.connection.writeLine(cmd);
                this.serialTextBoxRx.appendText("Send: " + cmd + "\n");
            } catch (ConnectionException ex) {
                serialTextBoxRx.appendText(ex.getMessage() + "\n");
            }
        }
    }

    private void clickSerialBtnConnect() {
        String com = serialNamesChoice.getValue();
        if(com != null) {
            switch (serialConnectBtn.getText()) {
                case "Connect":
                    boolean res = this.connect(com);
                    if (res) {
                        serialConnectBtn.setText("Disconnect");
                    }
                    break;

                case "Disconnect":
                    res = this.diconnect();
                    if (res) {
                        serialConnectBtn.setText("Connect");
                    }
                    break;
            }
        }
    }

    private boolean connect(String com) {
        SerialPort serial = new SerialPort(com);
        try {
            this.connection = new SerialConnection(serial);
            int baudrate = serialBaudrateChoice.getValue();
            this.connection.setSerialParams(
                    baudrate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            this.connection.setEOL(this.constructEOFBytes(serialEndOfLine.getValue()));
            this.connection.addListener(new Listener(serial));
            return true;
        } catch (ConnectionException ex) {
            serialTextBoxRx.appendText(ex.getMessage() + "\n");
            return false;
        }
    }

    private boolean diconnect() {
        if(this.connection != null) {
            try {
                this.connection.close();
                return true;
            } catch (ConnectionException ex) {
                serialTextBoxRx.appendText(ex.getMessage() + "\n");
                return false;
            }
        }
        return false;
    }

    private byte[] constructEOFBytes(String value) {
        switch (value) {
            case "NONE":
                return null;
            case "CR":
                return new byte[]{0x0D};
            case "LF":
                return new byte[]{0x0A};
            case "LFCR":
                return new byte[]{0x0A, 0x0D};
            case "CRLF":
                return new byte[]{0x0D, 0x0A};
        }
        return null;
    }

    private class Listener implements SerialPortEventListener {
        private SerialPort lport;
        public Listener(SerialPort lport) {
            this.lport = lport;
        }
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String data = lport.readString(event.getEventValue());
                    serialTextBoxRx.appendText(data);
                } catch (SerialPortException ex) {
                    serialTextBoxRx.appendText(ex.getMessage());
                }
            } else if(event.isBREAK()) {
                serialConnectBtn.setText("Connect");
                serialTextBoxRx.appendText("Closed\n");
            }
        }
    }

}
