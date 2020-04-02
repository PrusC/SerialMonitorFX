package connection;

public class ConnectionException extends Exception {

    public ConnectionException(String message, Object... params) {
        super(String.format(message, params));
    }
}
