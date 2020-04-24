package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FileDownloadException extends PlatformException {

    public FileDownloadException(String message) {
        super(message);
    }

    public FileDownloadException(String message, Throwable exception) {
        super(message, exception);
    }
}
