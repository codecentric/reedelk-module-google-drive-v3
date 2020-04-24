package com.reedelk.google.drive.v3.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class CreateFolderException extends PlatformException {

    public CreateFolderException(String message) {
        super(message);
    }

    public CreateFolderException(String message, Throwable exception) {
        super(message, exception);
    }
}
