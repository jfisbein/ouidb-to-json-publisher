package com.sputnik.ouidb.exception;

public class NoRecordsFoundException extends RuntimeException {

    public NoRecordsFoundException() {
        super();
    }

    public NoRecordsFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRecordsFoundException(String message) {
        super(message);
    }

    public NoRecordsFoundException(Throwable cause) {
        super(cause);
    }
}

