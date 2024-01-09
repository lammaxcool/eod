package org.kpi.processor.postgres;

public class ProcessingException extends RuntimeException {

    public ProcessingException(Throwable cause) {
        super(cause);
    }
}