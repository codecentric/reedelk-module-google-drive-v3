package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FolderCreateException extends PlatformException {

    public FolderCreateException(String message) {
        super(message);
    }

    public FolderCreateException(String message, Throwable exception) {
        super(message, exception);
    }
}
