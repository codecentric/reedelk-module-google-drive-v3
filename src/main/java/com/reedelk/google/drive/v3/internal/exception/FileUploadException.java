package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FileUploadException extends PlatformException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable exception) {
        super(message, exception);
    }
}
