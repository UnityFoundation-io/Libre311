package app.exception;

import io.micronaut.http.HttpStatus;

public class Libre311BaseException extends RuntimeException {
    private final HttpStatus status;
    private final String logref = LogrefGenerator.generate(5);

    public Libre311BaseException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
    public Libre311BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getLogref() {
        return logref;
    }
}
