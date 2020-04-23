package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FileUpdateContentException extends PlatformException {

    public FileUpdateContentException(String message) {
        super(message);
    }

    public FileUpdateContentException(String message, Throwable exception) {
        super(message, exception);
    }
}
