package com.reedelk.google.drive.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FileCreateException extends PlatformException {

    public FileCreateException(String message) {
        super(message);
    }

    public FileCreateException(String message, Throwable exception) {
        super(message, exception);
    }
}
