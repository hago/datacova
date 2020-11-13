package com.hagoapp.datacova;

/**
 * Exception to present anomaly situation given by DataCoVa itself.
 */
public class CoVaException extends Exception {

    public CoVaException() {
        super();
    }

    public CoVaException(String message) {
        super(message);
    }

    public CoVaException(String message, Throwable cause) {
        super(message, cause);
    }

}
