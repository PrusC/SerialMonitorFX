package connection;

import java.util.EventObject;

public class ConnectionDataEvent extends EventObject {

//    private static final long serialVersionUI
    private String data;

    public ConnectionDataEvent(Object source) {
        super(source);
        this.data = "";
    }

    public ConnectionDataEvent(Object source, String text) {
        super(source);
        this.data = text;
    }

    public ConnectionDataEvent(String text) {
        this(null, text);
    }

    public String getData() {
        return this.data;
    }
}
