package com.gunnarro.android.ughme.exception;

public class ApplicationException extends RuntimeException {

    public ApplicationException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}

