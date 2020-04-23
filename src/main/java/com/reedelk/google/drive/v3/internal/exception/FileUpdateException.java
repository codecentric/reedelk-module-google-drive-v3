package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FileUpdateException extends PlatformException {

    public FileUpdateException(String message) {
        super(message);
    }

    public FileUpdateException(String message, Throwable exception) {
        super(message, exception);
    }
}
