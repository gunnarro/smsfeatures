package com.gunnarro.android.ughme.exception;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ApplicationException extends RuntimeException {

    public ApplicationException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}

