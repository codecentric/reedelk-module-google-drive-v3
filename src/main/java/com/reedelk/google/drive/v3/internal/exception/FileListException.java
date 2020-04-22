package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FileListException extends PlatformException {

    public FileListException(String message) {
        super(message);
    }

    public FileListException(String message, Throwable exception) {
        super(message, exception);
    }
}
